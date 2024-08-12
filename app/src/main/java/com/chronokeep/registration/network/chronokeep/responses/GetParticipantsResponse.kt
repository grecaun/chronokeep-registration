package com.chronokeep.registration.network.chronokeep.responses

import com.chronokeep.registration.network.chronokeep.objects.ChronokeepEvent
import com.chronokeep.registration.network.chronokeep.objects.ChronokeepEventYear
import com.chronokeep.registration.network.chronokeep.objects.ChronokeepParticipant

class GetParticipantsResponse (
    val event: ChronokeepEvent,
    val year: ChronokeepEventYear,
    val participants: List<ChronokeepParticipant>
)