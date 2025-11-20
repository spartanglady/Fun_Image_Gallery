# Setup Checklist for Development

Use this checklist to ensure your development environment is properly configured.

## Prerequisites

- [ ] Java 17+ installed
- [ ] Node.js 18+ installed
- [ ] npm installed
- [ ] Git installed
- [ ] IDE/Editor of choice installed

## Backend Setup

### 1. Environment Configuration
- [ ] Copy `.env.example` to `.env`
  ```bash
  cp backend/.env.example backend/.env
  ```
- [ ] Review and customize `.env` settings if needed
- [ ] Verify storage paths are accessible

### 2. Gradle Verification
- [ ] Verify Gradle wrapper is executable
  ```bash
  cd backend
  chmod +x gradlew
  ```
- [ ] Test Gradle build
  ```bash
  ./gradlew build
  ```

### 3. Application Configuration
- [ ] Review `application.properties` (to be created by backend team)
- [ ] Configure H2 database settings
- [ ] Set up CORS configuration (see `docs/cors-config.md`)
- [ ] Configure file upload size limits

### 4. Dependencies
- [ ] Add Thumbnailator dependency
  ```gradle
  implementation 'net.coobird:thumbnailator:0.4.19'
  ```
- [ ] Add Metadata Extractor dependency
  ```gradle
  implementation 'com.drewnoakes:metadata-extractor:2.18.0'
  ```
- [ ] Add Spring Boot dependencies (Web, JPA, H2)

### 5. Run Backend
- [ ] Start the backend application
  ```bash
  ./gradlew bootRun
  ```
- [ ] Verify backend is running at `http://localhost:8080`
- [ ] Check H2 console at `http://localhost:8080/h2-console`

## Frontend Setup

### 1. Environment Configuration
- [ ] Copy `.env.example` to `.env`
  ```bash
  cp frontend/.env.example frontend/.env
  ```
- [ ] Verify `VITE_API_BASE_URL` points to backend
- [ ] Review other environment settings

### 2. Install Dependencies
- [ ] Install NPM packages
  ```bash
  cd frontend
  npm install
  ```
- [ ] Verify all dependencies installed successfully

### 3. Additional Dependencies
If not already in package.json, install:
- [ ] TanStack Query
  ```bash
  npm install @tanstack/react-query
  ```
- [ ] react-virtuoso
  ```bash
  npm install react-virtuoso
  ```
- [ ] react-dropzone
  ```bash
  npm install react-dropzone
  ```

### 4. Run Frontend
- [ ] Start the development server
  ```bash
  npm run dev
  ```
- [ ] Verify frontend is running at `http://localhost:5173`
- [ ] Check browser console for errors

## Directory Structure Verification

### 1. Required Directories Exist
- [ ] `backend/` exists
- [ ] `frontend/` exists
- [ ] `uploads/original/` exists
- [ ] `uploads/thumbnails/` exists
- [ ] `uploads/previews/` exists
- [ ] `docs/` exists

### 2. Runtime Directories (Created Automatically)
- [ ] `data/` will be created on first backend run
- [ ] `backend/build/` will be created on build
- [ ] `frontend/dist/` will be created on build

## Configuration Files

### 1. Root Level
- [ ] `.gitignore` exists and properly configured
- [ ] `README.md` exists
- [ ] `Project_Specification.md` exists
- [ ] `claude.md` exists

### 2. Backend
- [ ] `backend/build.gradle` exists
- [ ] `backend/settings.gradle` exists
- [ ] `backend/.env.example` exists
- [ ] `backend/.env` created (not in git)

### 3. Frontend
- [ ] `frontend/package.json` exists
- [ ] `frontend/vite.config.ts` exists
- [ ] `frontend/tsconfig.json` exists
- [ ] `frontend/tailwind.config.js` exists
- [ ] `frontend/.env.example` exists
- [ ] `frontend/.env` created (not in git)

### 4. Documentation
- [ ] `docs/API.md` exists
- [ ] `docs/cors-config.md` exists
- [ ] `docs/PROJECT_STRUCTURE.md` exists
- [ ] `docs/SETUP_CHECKLIST.md` exists (this file)

## Testing the Setup

### 1. Backend Health Check
- [ ] Backend starts without errors
- [ ] Can access `http://localhost:8080`
- [ ] H2 console accessible
- [ ] No port conflicts (8080 not in use)

### 2. Frontend Health Check
- [ ] Frontend starts without errors
- [ ] Can access `http://localhost:5173`
- [ ] No port conflicts (5173 not in use)
- [ ] Tailwind CSS working

### 3. Integration Check
- [ ] Frontend can make requests to backend
- [ ] CORS is properly configured
- [ ] No CORS errors in browser console

### 4. Storage Check
- [ ] `uploads/` directory writable
- [ ] Backend can create files in `uploads/`
- [ ] Sufficient disk space available

## Common Issues and Solutions

### Backend Won't Start

**Issue:** Port 8080 already in use
```
Solution: Change port in backend/.env
SERVER_PORT=8081
```

**Issue:** Gradle permission denied
```
Solution: Make gradlew executable
chmod +x backend/gradlew
```

**Issue:** Java version incorrect
```
Solution: Verify Java 17+ is installed
java -version
```

### Frontend Won't Start

**Issue:** Port 5173 already in use
```
Solution: Vite will automatically try 5174
Or set custom port in vite.config.ts
```

**Issue:** Node modules not found
```
Solution: Delete node_modules and reinstall
rm -rf frontend/node_modules
npm install
```

**Issue:** TypeScript errors
```
Solution: Ensure tsconfig.json is properly configured
Check for conflicting TypeScript versions
```

### CORS Errors

**Issue:** "No 'Access-Control-Allow-Origin' header"
```
Solution: Review docs/cors-config.md
Ensure backend CORS config allows http://localhost:5173
```

**Issue:** Preflight request fails
```
Solution: Check backend handles OPTIONS requests
Verify allowed methods include required HTTP verbs
```

### Upload Directory Issues

**Issue:** Permission denied when writing to uploads/
```
Solution: Check directory permissions
chmod -R 755 uploads/
```

**Issue:** Uploads directory not found
```
Solution: Verify path in backend/.env is correct
Ensure directory exists before starting backend
```

## Next Steps After Setup

### Backend Development
1. [ ] Create Photo entity
2. [ ] Create PhotoRepository
3. [ ] Create FileStorageService
4. [ ] Create ImageProcessingService
5. [ ] Create PhotoController
6. [ ] Implement API endpoints (see docs/API.md)

### Frontend Development
1. [ ] Set up API client
2. [ ] Create PhotoGrid component
3. [ ] Create PhotoCard component
4. [ ] Create Lightbox component
5. [ ] Create UploadZone component
6. [ ] Create SearchBar component

## Documentation References

- **API Endpoints**: See `docs/API.md`
- **CORS Setup**: See `docs/cors-config.md`
- **Project Structure**: See `docs/PROJECT_STRUCTURE.md`
- **Full Specification**: See `Project_Specification.md`
- **Claude Context**: See `claude.md`

## Support

If you encounter issues not covered in this checklist:
1. Check the project documentation in `docs/`
2. Review error logs in console/terminal
3. Verify all environment variables are set correctly
4. Ensure all prerequisites are met

---

**Last Updated:** 2025-11-19
**Checklist Version:** 1.0.0
