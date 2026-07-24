# selenium-project

## Getting started

* docker compose up -d jenkins (start container)
* (optional) docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword (4b9934a1bbdc4768b8b81652c7d44605)
* docker compose rm -f jenkins (remove container)
* docker compose stop (stop container)
* docker ps (check containers)

## GIT Managing

* git status
* git add .
* git commit -m "<paste-commit-message-here>"
* git push# Cukes Automation Framework 3

## Project cleanup

* ./gradlew --stop
* docker compose stop
* open codespaces -> Stop Current Codespace
