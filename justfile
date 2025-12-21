# list all available recipes
default:
  just --list

# run unit tests
test:
  ./gradlew test javadoc

# increment the provided version type and publish the repository
release type='patch': test
  ./gradlew release -P{{type}}
  git push origin tag $(git describe --tags --abbrev=0)
  ./gradlew publish