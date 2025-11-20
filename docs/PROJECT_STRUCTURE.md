# Project Structure

This document provides an overview of the Fun Image Gallery project structure.

## Directory Tree

```
Fun_Image_Gallery/
├── backend/                          # Spring Boot Backend Application
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/gallery/     # Java source code
│   │   │   └── resources/            # Application resources
│   │   └── test/                     # Test code
│   ├── gradle/                       # Gradle wrapper files
│   ├── build.gradle                  # Gradle build configuration
│   ├── settings.gradle               # Gradle settings
│   ├── gradlew                       # Gradle wrapper script (Unix)
│   └── .env.example                  # Backend environment template
│
├── frontend/                         # React Frontend Application
│   ├── src/                          # React source code
│   ├── public/                       # Static assets
│   ├── node_modules/                 # NPM dependencies (gitignored)
│   ├── package.json                  # NPM configuration
│   ├── package-lock.json             # NPM lock file
│   ├── vite.config.ts                # Vite configuration
│   ├── tsconfig.json                 # TypeScript configuration
│   ├── tailwind.config.js            # Tailwind CSS configuration
│   ├── eslint.config.js              # ESLint configuration
│   └── .env.example                  # Frontend environment template
│
├── uploads/                          # Photo Storage (gitignored)
│   ├── original/                     # Original uploaded photos
│   │   └── {yyyy}/{mm}/              # Date-based organization
│   ├── thumbnails/                   # 300px thumbnails
│   └── previews/                     # 1280px previews
│
├── docs/                             # Project Documentation
│   ├── API.md                        # API endpoint reference
│   ├── cors-config.md                # CORS configuration guide
│   └── PROJECT_STRUCTURE.md          # This file
│
├── data/                             # H2 Database Files (gitignored, created at runtime)
│   └── photogallery.mv.db            # H2 database file
│
├── .gitignore                        # Git ignore rules
├── README.md                         # Project overview and setup guide
├── Project_Specification.md          # Complete technical specification
└── claude.md                         # Claude AI context and conventions
```

## Key Directories Explained

### `/backend`
Spring Boot application handling:
- Photo upload and storage
- EXIF metadata extraction
- Image resizing (thumbnails & previews)
- Database operations (H2)
- REST API endpoints

**Key Files:**
- `build.gradle` - Gradle dependencies and build configuration
- `.env.example` - Environment variable template (copy to `.env`)

### `/frontend`
React + TypeScript application providing:
- Drag-and-drop photo upload
- Virtualized photo grid
- Lightbox viewer
- Search and filtering UI
- Tag management

**Key Files:**
- `package.json` - NPM dependencies
- `vite.config.ts` - Vite build tool configuration
- `tailwind.config.js` - Tailwind CSS styling configuration
- `.env.example` - Environment variable template (copy to `.env`)

### `/uploads`
Runtime photo storage organized for performance:
- **original/**: Full resolution photos organized by upload date (`yyyy/mm/uuid.jpg`)
- **thumbnails/**: 300px thumbnails for grid display (`uuid.jpg`)
- **previews/**: 1280px previews for lightbox view (`uuid.jpg`)

Note: This directory is gitignored and created at runtime.

### `/docs`
Project documentation:
- **API.md**: Complete API endpoint reference with request/response examples
- **cors-config.md**: CORS setup guide for Spring Boot
- **PROJECT_STRUCTURE.md**: This file - project structure overview

### `/data`
H2 database storage (created at runtime, gitignored):
- Contains the H2 file-based database
- Persists photo metadata between application restarts

## File Purposes

### Root Level Files

| File | Purpose |
|------|---------|
| `.gitignore` | Specifies files/directories to exclude from git |
| `README.md` | Project overview, setup instructions, and quick reference |
| `Project_Specification.md` | Detailed technical specification |
| `claude.md` | Context file for Claude AI development assistant |

### Backend Files

| File | Purpose |
|------|---------|
| `build.gradle` | Gradle build configuration with dependencies |
| `settings.gradle` | Gradle project settings |
| `gradlew` | Gradle wrapper script (Unix/Mac) |
| `.env.example` | Environment variable template |

### Frontend Files

| File | Purpose |
|------|---------|
| `package.json` | NPM dependencies and scripts |
| `vite.config.ts` | Vite build configuration |
| `tsconfig.json` | TypeScript compiler settings |
| `tailwind.config.js` | Tailwind CSS customization |
| `eslint.config.js` | Code linting rules |
| `.env.example` | Environment variable template |

## Environment Configuration

Both backend and frontend have `.env.example` files that should be copied to `.env` and customized:

### Backend Environment Variables
```bash
cp backend/.env.example backend/.env
```

Key settings:
- Storage paths
- Database configuration
- CORS allowed origins
- File upload limits

### Frontend Environment Variables
```bash
cp frontend/.env.example frontend/.env
```

Key settings:
- API base URL
- Upload limits
- Feature flags
- Performance settings

## Gitignored Items

The following are excluded from version control:

### Build Artifacts
- `backend/build/`
- `backend/.gradle/`
- `backend/target/`
- `frontend/node_modules/`
- `frontend/dist/`

### Runtime Generated
- `uploads/` (all photo files)
- `data/` (H2 database files)

### Environment Files
- `.env` files (all)

### IDE Files
- `.idea/` (IntelliJ IDEA)
- `.vscode/` (VS Code)
- `*.iml` (IntelliJ modules)

## Development Workflow

### Initial Setup
1. Copy environment templates:
   ```bash
   cp backend/.env.example backend/.env
   cp frontend/.env.example frontend/.env
   ```

2. Start backend:
   ```bash
   cd backend
   ./gradlew bootRun
   ```

3. Start frontend:
   ```bash
   cd frontend
   npm install
   npm run dev
   ```

### Access Points
- **Frontend**: http://localhost:5173
- **Backend API**: http://localhost:8080/api
- **H2 Console**: http://localhost:8080/h2-console

## Data Flow

```
User Upload
    ↓
Frontend (React)
    ↓ (HTTP POST)
Backend API (/api/photos/upload)
    ↓
Image Processing Service
    ├── Save Original → uploads/original/{yyyy}/{mm}/{uuid}.jpg
    ├── Generate Thumbnail → uploads/thumbnails/{uuid}.jpg
    └── Generate Preview → uploads/previews/{uuid}.jpg
    ↓
EXIF Extraction
    ↓
Save Metadata → H2 Database
    ↓
Return Photo DTO → Frontend
```

## Storage Estimates

Based on typical photo sizes:

| Photo Type | Avg Size | 100 Photos | 1000 Photos | 10000 Photos |
|------------|----------|------------|-------------|--------------|
| Original   | 5 MB     | 500 MB     | 5 GB        | 50 GB        |
| Preview    | 500 KB   | 50 MB      | 500 MB      | 5 GB         |
| Thumbnail  | 50 KB    | 5 MB       | 50 MB       | 500 MB       |
| **Total**  | **5.5 MB** | **555 MB** | **5.5 GB**  | **55 GB**    |

Database size is negligible compared to image storage (typically <1% of total).

## Port Configuration

Default ports (configurable via environment variables):

- **Frontend Dev Server**: 5173 (Vite default)
- **Backend API**: 8080 (Spring Boot default)
- **H2 Console**: 8080/h2-console

## Next Steps for Development

### Backend Team
1. Review `docs/API.md` for endpoint specifications
2. Review `docs/cors-config.md` for CORS setup
3. Implement domain models, repositories, services, and controllers
4. Configure application.properties with environment variables
5. Add Thumbnailator and Metadata Extractor dependencies

### Frontend Team
1. Review `docs/API.md` for API contracts
2. Set up API client with axios/fetch
3. Implement components (PhotoGrid, PhotoCard, Lightbox, UploadZone)
4. Configure React Query with proper caching
5. Implement virtualization with react-virtuoso

## Additional Resources

- Full specification: [Project_Specification.md](../Project_Specification.md)
- API reference: [API.md](API.md)
- CORS setup: [cors-config.md](cors-config.md)
- Claude context: [claude.md](../claude.md)
