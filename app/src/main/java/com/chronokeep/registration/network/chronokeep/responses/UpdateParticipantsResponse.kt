package com.chronokeep.registration.network.chronokeep.responses

import com.chronokeep.registration.network.chronokeep.objects.ChronokeepParticipant

class UpdateParticipantsResponse (
    val participants: List<ChronokeepParticipant>,
    val updated_participants: List<ChronokeepParticipant>,
)