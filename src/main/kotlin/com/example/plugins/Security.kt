package com.example.plugins

import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import com.example.config.TokenConfig
import com.example.services.token.TokenService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import org.koin.core.parameter.parametersOf
import org.koin.ktor.ext.get
import org.koin.ktor.ext.inject

fun Application.configureSecurity() {


    val tokenConfigParams = mapOf<String, String>(
        "audience" to environment.config.property("jwt.audience").getString(),
        "secret" to environment.config.property("jwt.secret").getString(),
        "issuer" to environment.config.property("jwt.issuer").getString(),
        "realm" to environment.config.property("jwt.realm").getString(),
        "expiration" to environment.config.property("jwt.expiration").getString()
    )

    val tokenConfig: TokenConfig = get { parametersOf(tokenConfigParams) }
    val tokenService: TokenService by inject()

    authentication {
        jwt {
            verifier(tokenService.verifyJWT())
            realm = tokenConfig.realm

            validate { credential ->
                if (credential.payload.audience.contains(tokenConfig.audience)&&
                    credential.payload.getClaim("nombre").asString().isNotEmpty())
                    JWTPrincipal(credential.payload)
                else null
            }
            challenge { defaultScheme, realm ->
                call.respond(HttpStatusCode.Unauthorized, "Token invalido o expirado")
            }

        }
    }
}
