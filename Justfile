# list all available recipes
default:
  just --list

# run the test
test:
  ./gradlew test

# publish a new version of the repository
publish version message:
  git tag -a v{{version}} -m "{{message}}"
  git push origin tag v{{version}}
  gh release create v{{version}} --verify-tag --notes-from-tag
  ./gradlew publish