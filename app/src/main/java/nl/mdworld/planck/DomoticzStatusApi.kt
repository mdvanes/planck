package nl.mdworld.planck

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.header

class DomoticzStatusApi {
    suspend fun getDomoticzStatusKtor(
        //userId: String
    ): DomoticzStatusEntity = ktorHttpClient.get("http://192.168.0.8:8080/json.htm?type=command&param=getversion")
    //): DomoticzStatusEntity = ktorHttpClient.get("$END_POINT_GET_USER_KTOR$userId")

    //suspend fun saveDomoticzStatus(user: UserEntity) {
    //    client.post<UserEntity>("$END_POINT_POST_USER_KTOR") {
    //        body = user
    //    }
    //}
}