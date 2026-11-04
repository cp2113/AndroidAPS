package app.aaps.pump.equil.di

import app.aaps.core.interfaces.configuration.Config
import app.aaps.core.interfaces.configuration.ExternalOptions
import app.aaps.core.interfaces.logging.AAPSLogger
import app.aaps.core.interfaces.logging.LTag
import app.aaps.core.keys.interfaces.Preferences
import app.aaps.pump.equil.ble.EquilBleTransport
import app.aaps.pump.equil.ble.EquilBleTransportImpl
import app.aaps.pump.equil.emulator.EquilEmulatorBleTransport
import app.aaps.pump.equil.emulator.EquilPumpEmulator
import app.aaps.pump.equil.keys.EquilStringKey
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@ContributesTo(AppScope::class)
@BindingContainer
object EquilTransportBindings {

    @Provides
    @SingleIn(AppScope::class)
    fun provideEquilBleTransport(
        config: Config,
        equilBleTransportImpl: EquilBleTransportImpl,
        aapsLogger: AAPSLogger,
        preferences: Preferences
    ): EquilBleTransport {
        return if (true || config.isEnabled(ExternalOptions.EMULATE_EQUIL)) {
            val pumpEmulator = EquilPumpEmulator(aapsLogger = aapsLogger)
            EquilEmulatorBleTransport(
                emulator = pumpEmulator,
                aapsLogger = aapsLogger,
                serialNumberProvider = {
                    // FIX sorozatszám a tesztekhez, hogy mindig ugyanaz legyen a BLE név és a titkosítás
                    val sn = "A00001"
                    preferences.put(EquilStringKey.EmulatorDeviceName, "Equil - $sn")
                    aapsLogger.debug(LTag.PUMPCOMM, "Equil emulator scan with serial: $sn")
                    sn
                },
                storedPasswordProvider = {
                    val pwd = preferences.get(EquilStringKey.Password)
                    if (pwd.isNotEmpty()) pwd else pumpEmulator.state.pairingPasswordKey
                }
            )
        } else {
            equilBleTransportImpl
        }
    }
}
