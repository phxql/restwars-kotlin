package restwars.rest.controller

import restwars.business.config.Config
import restwars.rest.api.ConfigResponse
import restwars.rest.base.Method
import restwars.rest.base.Result
import spark.Request
import spark.Response

class ConfigurationController(val config: Config) {
    fun get(): Method {
        return object : Method {
            override fun invoke(req: Request, res: Response): Result {
                return ConfigResponse.fromConfig(config)
            }
        }
    }
}