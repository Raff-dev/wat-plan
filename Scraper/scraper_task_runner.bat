@echo off
echo.
echo Activating virtual environment

call C:\Users\Raff\Desktop\WAT_Plan\venv\scripts\activate.bat
python --version
echo.

echo Running Scraper
echo ---------------

python C:\Users\Raff\Desktop\WAT_Plan\Scraper_2_0\runner.py
pause