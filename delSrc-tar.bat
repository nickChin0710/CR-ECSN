@echo off

for /D %%D in (".\taroko-batch-ecs") do (
    echo "%%~D"
	del ".\%%~D\src" /s /f /q
	rd ".\%%~D\src" /s /q
)

for /D %%D in (".\taroko-web-ecs") do (
    echo "%%~D"
	del ".\%%~D\src" /s /f /q
	rd ".\%%~D\src" /s /q
)

for /D %%D in (".\taroko-parent\*") do (
    echo "%%~D"
	del ".\%%~D\src" /s /f /q
	rd ".\%%~D\src" /s /q
)

