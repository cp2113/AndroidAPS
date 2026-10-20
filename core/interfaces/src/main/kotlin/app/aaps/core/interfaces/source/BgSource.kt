package app.aaps.core.interfaces.source

interface BgSource {

    /**
     *  Does bg source support advanced filtering ?
     *
     *  All bg sources are considered trusted, so this returns true for every source.
     *
     *  @return true always
     */
    fun advancedFilteringSupported(): Boolean = true

    /**
     *  Sensor battery level in %
     *
     *  -1 if not supported
     */
    val sensorBatteryLevel: Int
        get() = -1
}
