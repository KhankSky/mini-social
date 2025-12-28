# Script build và push Docker image cho Frontend

$ErrorActionPreference = "Stop"

# 1. Nhập thông tin Image
$DockerUser = "khanksky"
$ImageName = "$DockerUser/social-fe"
$Tag = "latest"
# Địa chỉ API Backend (quan trọng để build vào code React)
$ApiUrl = "http://192.168.1.202:9090/api"

Write-Host "`n=== BAT DAU BUILD IMAGE: ${ImageName}:${Tag} ===`n" -ForegroundColor Cyan
Write-Host "API URL se duoc build cung: $ApiUrl" -ForegroundColor Yellow

# 2. Build Image (truyền biến môi trường vào lúc build)
docker build --build-arg VITE_API_BASE_URL=$ApiUrl -t "${ImageName}:${Tag}" .

if ($LASTEXITCODE -eq 0) {
    Write-Host "`n=== BUILD THANH CONG. DANG PUSH LEN DOCKER HUB... ===`n" -ForegroundColor Green
    
    # 3. Push Image
    docker push "${ImageName}:${Tag}"
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "`n=== PUSH THANH CONG! ===`n" -ForegroundColor Green
        Write-Host "Bay gio ban co the copy file 'docker-compose.fe.yml' len App Server va chay lenh sau:" -ForegroundColor Yellow
        Write-Host "export DOCKER_IMAGE_FE=${ImageName}:${Tag}"
        Write-Host "docker-compose -f docker-compose.fe.yml up -d"
    } else {
        Write-Host "`n!!! LOI KHI PUSH IMAGE. HAY KIEM TRA LAI 'docker login' !!!`n" -ForegroundColor Red
    }
} else {
    Write-Host "`n!!! LOI KHI BUILD IMAGE !!!`n" -ForegroundColor Red
}
