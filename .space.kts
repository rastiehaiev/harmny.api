job("Build") {
    container(displayName = "Build", image = "gradle:6.3-jdk11") {
        kotlinScript { api ->
            api.gradlew("build")
        }
    }
}