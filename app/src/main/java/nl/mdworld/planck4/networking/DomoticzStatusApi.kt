package nl.mdworld.planck4.networking

import io.ktor.client.call.body
import io.ktor.client.request.get

class DomoticzStatusApi {
    suspend fun getDomoticzStatusKtor(
        //userId: String
    ): DomoticzStatusEntity = ktorHttpClient.get("http://192.168.0.8:8080/json.htm?type=command&param=getversion").body()
    //): DomoticzStatusEntity = ktorHttpClient.get("$END_POINT_GET_USER_KTOR$userId")

    //suspend fun saveDomoticzStatus(user: UserEntity) {
    //    client.post<UserEntity>("$END_POINT_POST_USER_KTOR") {
    //        body = user
    //    }
    //}
}