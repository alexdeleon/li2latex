package li2latex.oauth

import li2latex.config.AppConfig
import org.scribe.builder.api.LinkedInApi
import org.scribe.oauth.OAuthService
import org.scribe.builder.ServiceBuilder
import org.scribe.exceptions.OAuthException
import org.scribe.model.{OAuthRequest, Verb, Token, Verifier}

trait OAuthClient {
  private lazy val builder = new ServiceBuilder
  private lazy val service: OAuthService =
    builder provider (new LinkedInApi) apiKey (AppConfig.API_KEY) apiSecret (AppConfig.API_SECRET) build()
  lazy val accessToken = getAccessToken

  private def getAccessToken: Option[Token] = {
    // Get the Request Token
    val reqToken = service.getRequestToken
    val authUrl = service.getAuthorizationUrl(reqToken)
    println("Please open the following authorization URL:\n")
    println(authUrl)
    println("\nPaste your verifier here:")
    val v = new Verifier(readLine())
    // Get the Access Token
    try {
      val accessToken = service.getAccessToken(reqToken, v)
      Some(accessToken)
    } catch {
      case e: OAuthException => None

    }
  }

  /*
  * A API URL, e.g. http://api.linkedin.com/v1/people/~:(educations)
  */
  val getByUrl: String => String =
    url => {
      val token = accessToken.get
      val req = new OAuthRequest(Verb.GET, url)
      service.signRequest(token, req)
      req.send.getBody
    }

  private val fieldToUrl: String => String =
    field => String.format(AppConfig.API_DATA_URL, field)

  /*
   * A profile field, e.g. educations
   */
  val getByField: String => String = fieldToUrl andThen getByUrl
}

object OAuthClientImpl extends OAuthClient