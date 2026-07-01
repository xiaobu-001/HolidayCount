@echo off

echo.
echo ==========================================
echo   HolidayCount - Push to GitHub
echo ==========================================
echo.
echo [Step 1] Checking git...

where git >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: git not found. Please install Git for Windows.
    pause
    exit /b 1
)
echo   git found: OK

echo.
echo [Step 2] Enter your GitHub repository URL
echo   Example: https://github.com/YourName/HolidayCount.git
echo   Visit https://github.com/new to create one if you haven't.
echo.
set /p REPO_URL=Repo URL: 

if "%REPO_URL%"=="" (
    echo ERROR: URL cannot be empty.
    pause
    exit /b 1
)

echo.
echo [Step 3] Init git repo if needed...
if not exist ".git" (
    git init
    git branch -M main
    echo   git init done.
) else (
    echo   already a git repo, skipping init.
)

echo.
echo [Step 4] Setting up .gitignore...
if not exist ".gitignore" (
    (
        echo *.iml
        echo .gradle
        echo /local.properties
        echo /.idea/
        echo .DS_Store
        echo /build
        echo app/build
        echo *.apk
        echo *.aab
    ) > .gitignore
    echo   .gitignore created.
) else (
    echo   .gitignore already exists, skipping.
)

echo.
echo [Step 5] Setting remote origin...
git remote get-url origin >nul 2>&1
if %errorlevel% equ 0 (
    git remote set-url origin %REPO_URL%
    echo   remote url updated.
) else (
    git remote add origin %REPO_URL%
    echo   remote origin added.
)

echo.
echo [Step 6] Staging all files...
git add -A
echo   all files staged.

echo.
echo [Step 7] Committing...
git commit -m "feat: HolidayCount Android holiday countdown widget app" 2>nul
if %errorlevel% neq 0 (
    echo   nothing new to commit, or commit failed.
)

echo.
echo [Step 8] Pushing to GitHub...
git push -u origin main
if %errorlevel% neq 0 (
    echo.
    echo   Push failed. Common reasons:
    echo   1. Wrong URL - check your repo URL
    echo   2. Not logged in - a browser login window should open
    echo   3. Branch conflict - try: git push -u origin main --force
    pause
    exit /b 1
)

echo.
echo ==========================================
echo   SUCCESS! Code pushed to GitHub.
echo.
echo   Next steps:
echo   1. Open your GitHub repo in browser
echo   2. Click the [Actions] tab
echo   3. Wait for "Build Debug APK" to finish (~5-10 min)
echo   4. Click the job -> download APK from Artifacts
echo ==========================================
pause
