package com.chronokeep.registration.util

object Constants {
    const val zero_conf_port = 4488
    const val zero_conf_multicast_address = "224.0.44.88"

    const val portal_setting_name = "SETTING_PORTAL_NAME"
    const val portal_setting_chip_type = "SETTING_CHIP_TYPE"
    const val portal_setting_read_window = "SETTING_READ_WINDOW"
    const val portal_setting_sighting_period = "SETTING_SIGHTING_PERIOD"
    const val portal_setting_play_sound = "SETTING_PLAY_SOUND"
    const val portal_setting_volume = "SETTING_VOLUME"
    const val portal_setting_voice = "SETTING_VOICE"
    const val portal_setting_upload_interval = "SETTING_UPLOAD_INTERVAL"

    const val portal_setting_chip_type_dec = "DEC"
    const val portal_setting_chip_type_hex = "HEX"

    const val portal_setting_voice_emily = "emily"
    const val portal_setting_voice_michael = "michael"
    const val portal_setting_voice_custom = "custom"

    const val antenna_status_none: Int = 0
    const val antenna_status_disconnected: Int = 1
    const val antenna_status_connected: Int = 2

    const val registration_type: String = "CHRONOKEEP_WINDOWS"

    const val setting_event_slug: String = "EVENT_SLUG"
    const val setting_event_year: String = "EVENT_YEAR"
    const val setting_username: String = "USERNAME"
    const val setting_auth_token: String = "AUTH_TOKEN"
    const val setting_refresh_token: String = "REFRESH_TOKEN"
    const val setting_save_username: String = "SAVE_USERNAME"

    const val setting_true: String = "TRUE"
    const val setting_false: String = "FALSE"
}