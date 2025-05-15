package com.chronokeep.registration.network.chronokeep.responses

import com.chronokeep.registration.network.chronokeep.objects.ChronokeepParticipant

class AddParticipantsResponse (
    val participants: List<ChronokeepParticipant>,
    val updated_participants: List<ChronokeepParticipant>,
)