import { useState, useCallback } from 'react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { uploadPhoto, checkDuplicate } from '../api/client';
import { calculateFileHashWithProgress } from '../utils/hash';
import type { UploadProgress } from '../types';

export const useUpload = () => {
  const [uploads, setUploads] = useState<Map<string, UploadProgress>>(new Map());
  const queryClient = useQueryClient();

  const uploadMutation = useMutation({
    mutationFn: (file: File) => uploadPhoto(file),
    onSuccess: () => {
      // Invalidate photos query to refresh the grid
      queryClient.invalidateQueries({ queryKey: ['photos'] });
    },
  });

  const updateUploadProgress = useCallback((fileId: string, update: Partial<UploadProgress>) => {
    setUploads((prev) => {
      const newMap = new Map(prev);
      const existing = newMap.get(fileId);
      if (existing) {
        newMap.set(fileId, { ...existing, ...update });
      }
      return newMap;
    });
  }, []);

  const uploadFiles = useCallback(
    async (files: File[]) => {
      const fileArray = Array.from(files);

      // Initialize upload progress for each file
      const initialUploads = new Map<string, UploadProgress>();
      fileArray.forEach((file) => {
        const fileId = `${file.name}-${file.size}-${Date.now()}`;
        initialUploads.set(fileId, {
          file,
          progress: 0,
          status: 'pending',
        });
      });
      setUploads(initialUploads);

      // Process each file
      for (const [fileId, uploadProgress] of initialUploads.entries()) {
        const { file } = uploadProgress;

        try {
          // Step 1: Hash the file
          updateUploadProgress(fileId, { status: 'hashing', progress: 0 });
          const hash = await calculateFileHashWithProgress(file, (progress) => {
            updateUploadProgress(fileId, { progress: progress / 2 }); // First 50% for hashing
          });

          // Step 2: Check for duplicates
          updateUploadProgress(fileId, { status: 'checking', progress: 50 });
          const duplicateCheck = await checkDuplicate({
            fileHash: hash,
            filename: file.name,
          });

          if (duplicateCheck.isDuplicate) {
            updateUploadProgress(fileId, {
              status: 'duplicate',
              progress: 100,
              duplicatePhotoId: duplicateCheck.existingPhotoId,
            });
            continue;
          }

          // Step 3: Upload the file
          updateUploadProgress(fileId, { status: 'uploading', progress: 50 });
          await uploadMutation.mutateAsync(file);

          // Mark as completed (the mutation onSuccess will invalidate queries)
          updateUploadProgress(fileId, {
            status: 'completed',
            progress: 100,
          });

        } catch (error) {
          updateUploadProgress(fileId, {
            status: 'error',
            error: error instanceof Error ? error.message : 'Upload failed',
          });
        }
      }
    },
    [uploadMutation, updateUploadProgress]
  );

  const clearCompleted = useCallback(() => {
    setUploads((prev) => {
      const newMap = new Map(prev);
      for (const [key, value] of newMap.entries()) {
        if (value.status === 'completed' || value.status === 'error' || value.status === 'duplicate') {
          newMap.delete(key);
        }
      }
      return newMap;
    });
  }, []);

  return {
    uploads: Array.from(uploads.values()),
    uploadFiles,
    clearCompleted,
    isUploading: uploadMutation.isPending,
  };
};
