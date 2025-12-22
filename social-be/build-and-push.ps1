# Script build và push Docker image trên Windows

$ErrorActionPreference = "Stop"

# 1. Nhập thông tin Image
$DockerUser = "khanksky"
$ImageName = "$DockerUser/social-be"
$Tag = "latest"

Write-Host "`n=== BAT DAU BUILD IMAGE: ${ImageName}:${Tag} ===`n" -ForegroundColor Cyan

# 2. Build Image
# Sử dụng Dockerfile hiện tại để build
docker build -t "${ImageName}:${Tag}" .

if ($LASTEXITCODE -eq 0) {
    Write-Host "`n=== BUILD THANH CONG. DANG PUSH LEN DOCKER HUB... ===`n" -ForegroundColor Green
    
    # 3. Push Image
    docker push "${ImageName}:${Tag}"
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "`n=== PUSH THANH CONG! ===`n" -ForegroundColor Green
        Write-Host "Bay gio ban co the copy file 'docker-compose.prod.yml' len App Server va chay lenh sau:" -ForegroundColor Yellow
        Write-Host "export DOCKER_IMAGE_NAME=${ImageName}:${Tag}"
        Write-Host "export DB_HOST=<IP_DATABASE_SERVER>"
        Write-Host "export DB_USER=root"
        Write-Host "export DB_PASSWORD=123456"
        Write-Host "docker-compose -f docker-compose.prod.yml up -d"
    } else {
        Write-Host "`n!!! LOI KHI PUSH IMAGE. HAY KIEM TRA LAI 'docker login' !!!`n" -ForegroundColor Red
    }
} else {
    Write-Host "`n!!! LOI KHI BUILD IMAGE !!!`n" -ForegroundColor Red
}
