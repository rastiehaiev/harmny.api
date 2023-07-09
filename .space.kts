job("Build & Publish to Docker Registry") {
    startOn {
        gitPush {
            branchFilter {
                +"refs/heads/main"
            }
        }
    }
    host("Build & Push Docker image") {

        // assign project secrets to environment variables
        env["SPACE_USER"] = Secrets("space_user")
        env["SPACE_TOKEN"] = Secrets("space_token")
        env["HARMNY_JWT_KEY"] = Secrets("jwt_token")

        shellScript {
            content = """
                docker login harmony.registry.jetbrains.space --username ${'$'}SPACE_USER --password "${'$'}SPACE_TOKEN"
            """
        }

        dockerBuildPush {
            tags {
                +"harmony.registry.jetbrains.space/p/harmny/containers/harmny.api:latest"
            }
        }
    }
}
