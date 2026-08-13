$ErrorActionPreference = "Stop"

Write-Host "=======================================================" -ForegroundColor Cyan
Write-Host "Building Docker Images for Airline Microservices" -ForegroundColor Cyan
Write-Host "Make sure Docker Desktop is running!" -ForegroundColor Yellow
Write-Host "=======================================================" -ForegroundColor Cyan

Write-Host "Step 1: Compiling the entire project and installing common-lib..." -ForegroundColor Green
mvn clean install -DskipTests

# Define the images to build mapping (Module Path -> Image Name)
# Note: These names exactly match your docker-compose.services.yml
$images = [ordered]@{
    "cloud/config-server" = "zosh/yt-gds-config-server:1.0.0"
    "cloud/service-registry" = "zosh/yt-gds-service-registry:1.0.0"
    "cloud/api-gateway" = "zosh/yt-gds-api-gateway:1.0.0"
    "services/user-service" = "zosh/yt-gds-user:1.0.0"
    "services/airline-core-service" = "zosh/yt-gds-airline:1.0.0"
    "services/flight-ops-service" = "zosh/yt-gds-flight:1.0.0"
    "services/location-service" = "zosh/yt-gds-location:1.0.0"
    "services/seat-service" = "zosh/yt-gds-seat:1.0.0"
    "services/pricing-service" = "zosh/yt-gds-pricing:1.0.0"
    "services/ancillary-service" = "zosh/yt-gds-ancillary:1.0.0"
    "services/booking-service" = "zosh/yt-gds-booking:1.0.0"
    "services/payment-service" = "zosh/yt-gds-payment:1.0.0"
    "services/notification-service" = "zosh/yt-gds-notification:1.0.0"
}

Write-Host "Step 2: Building individual Docker images via Cloud Native Buildpacks..." -ForegroundColor Green

foreach ($module in $images.Keys) {
    $imageName = $images[$module]
    Write-Host "`n>>> Building image for $module -> $imageName" -ForegroundColor Cyan
    
    # Run Maven Jib for each specific module
    mvn -pl $module compile jib:dockerBuild "-Djib.to.image=$imageName" "-DskipTests"
    
    if ($LASTEXITCODE -ne 0) {
        Write-Host "ERROR: Failed to build image for $module!" -ForegroundColor Red
        exit $LASTEXITCODE
    }
}

Write-Host "`n=======================================================" -ForegroundColor Green
Write-Host "SUCCESS: All Docker images built successfully!" -ForegroundColor Green
Write-Host "You can now cd into docker-compose and run: docker compose -f docker-compose.services.yml up -d" -ForegroundColor Green
Write-Host "=======================================================" -ForegroundColor Green
