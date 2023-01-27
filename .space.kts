job("Build") {
    startOn {
        gitPush {
            branchFilter {
                +"refs/heads/main"
            }
        }
    }
    container(displayName = "Build", image = "gradle:6.3-jdk11") {
        kotlinScript { api ->
            api.gradlew("build")
        }
    }
}
