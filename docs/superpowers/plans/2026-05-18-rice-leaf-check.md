# RiceLeafCheck Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build complete rice leaf disease inspection system: Java Spring Boot backend + Android (Jetpack Compose) client, mock detection initially.

**Architecture:** Spring Boot REST API → Controller → Service(strategy) → Repository(JPA) → H2/MySQL. Android: CameraX + Jetpack Compose + Retrofit + Room + Hilt. Three tables: disease_dict, record, image.

**Tech Stack:** Java 17, Spring Boot 3.2, Spring Data JPA, H2, Maven, Kotlin, Jetpack Compose, CameraX, Retrofit, Room, Hilt, Coil

**Key design note:** User changed flow — take photo FIRST, then fill parameters, then upload.

---

## Phase 1: Backend Core

### Task 1: Project scaffolding
- Create: `rice-leaf-server/pom.xml`
- Create: `rice-leaf-server/src/main/java/com/riceleaf/RiceLeafApplication.java`
- Create: `rice-leaf-server/src/main/resources/application.yml`
- Create: `rice-leaf-server/src/main/resources/application-dev.yml`

### Task 2: Entity classes
- Create: `rice-leaf-server/src/main/java/com/riceleaf/entity/DiseaseDict.java`
- Create: `rice-leaf-server/src/main/java/com/riceleaf/entity/Record.java`
- Create: `rice-leaf-server/src/main/java/com/riceleaf/entity/Image.java`

### Task 3: Repository interfaces
- Create: `rice-leaf-server/src/main/java/com/riceleaf/repository/DiseaseDictRepository.java`
- Create: `rice-leaf-server/src/main/java/com/riceleaf/repository/RecordRepository.java`
- Create: `rice-leaf-server/src/main/java/com/riceleaf/repository/ImageRepository.java`

### Task 4: DTO classes
- Create: `rice-leaf-server/src/main/java/com/riceleaf/dto/ApiResponse.java`
- Create: `rice-leaf-server/src/main/java/com/riceleaf/dto/DetectResponse.java`
- Create: `rice-leaf-server/src/main/java/com/riceleaf/dto/RecordListResponse.java`
- Create: `rice-leaf-server/src/main/java/com/riceleaf/dto/RecordDetailResponse.java`
- Create: `rice-leaf-server/src/main/java/com/riceleaf/dto/RecordUpdateRequest.java`

### Task 5: Strategy pattern - Detection
- Create: `rice-leaf-server/src/main/java/com/riceleaf/service/strategy/DetectResult.java`
- Create: `rice-leaf-server/src/main/java/com/riceleaf/service/strategy/DiseaseDetectStrategy.java`
- Create: `rice-leaf-server/src/main/java/com/riceleaf/service/strategy/MockDiseaseDetectStrategy.java`

### Task 6: Service layer
- Create: `rice-leaf-server/src/main/java/com/riceleaf/service/DiseaseService.java`
- Create: `rice-leaf-server/src/main/java/com/riceleaf/service/RecordService.java`

### Task 7: Controller layer
- Create: `rice-leaf-server/src/main/java/com/riceleaf/controller/DetectController.java`
- Create: `rice-leaf-server/src/main/java/com/riceleaf/controller/DiseaseController.java`
- Create: `rice-leaf-server/src/main/java/com/riceleaf/controller/RecordController.java`

### Task 8: Utilities, config, exception handling, seed data
- Create: `rice-leaf-server/src/main/java/com/riceleaf/util/ImageUtil.java`
- Create: `rice-leaf-server/src/main/java/com/riceleaf/util/ExcelUtil.java`
- Create: `rice-leaf-server/src/main/java/com/riceleaf/config/WebConfig.java`
- Create: `rice-leaf-server/src/main/java/com/riceleaf/exception/GlobalExceptionHandler.java`
- Create: `rice-leaf-server/src/main/resources/data.sql`

---

## Phase 2: Android App

### Task 9: Android project scaffolding
- Create Gradle build files, AndroidManifest.xml, Application class

### Task 10: Data layer - Remote (Retrofit)
- Create API service interface, Retrofit client, network DTOs

### Task 11: Data layer - Local (Room)
- Create Room database, entities, DAOs

### Task 12: Repository + Domain models
- Create RecordRepository, domain model classes

### Task 13: DI module (Hilt)
- Create AppModule with all provider methods

### Task 14: MainActivity + Bottom Navigation
- Create MainActivity with 3-tab BottomNavigationView + NavHost

### Task 15: Camera screen
- Create CameraScreen (Compose + CameraX), CameraViewModel
- Flow: preview → capture → fill params → upload → result card

### Task 16: Data list + detail screens
- Create DataListScreen, DataDetailScreen, DataViewModel

### Task 17: Settings + Disease Reference + Usage Guide
- Create SettingsScreen, DiseaseReferenceScreen, UsageGuideScreen

### Task 18: Utilities
- Create ImageCompressor, FileUtil

---

## Phase 3: Polish (deferred)
- Offline Room caching, pull-to-refresh, error retry, loading states
