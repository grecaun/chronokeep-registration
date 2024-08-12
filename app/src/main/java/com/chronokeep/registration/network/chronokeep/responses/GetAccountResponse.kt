package com.chronokeep.registration.network.chronokeep.responses

import com.chronokeep.registration.network.chronokeep.objects.ChronokeepAccount
import com.chronokeep.registration.network.chronokeep.objects.ChronokeepEvent
import com.chronokeep.registration.network.chronokeep.objects.ChronokeepKey

class GetAccountResponse (
    val account: ChronokeepAccount,
    val keys: List<ChronokeepKey>,
    val events: List<ChronokeepEvent>,
    val linked: List<ChronokeepAccount>
)