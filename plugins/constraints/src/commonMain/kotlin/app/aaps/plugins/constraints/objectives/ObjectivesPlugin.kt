package app.aaps.plugins.constraints.objectives

import app.aaps.core.data.plugin.PluginType
import app.aaps.core.interfaces.configuration.Config
import app.aaps.core.interfaces.constraints.Constraint
import app.aaps.core.interfaces.constraints.Objectives
import app.aaps.core.interfaces.constraints.Objectives.Companion.AUTOSENS_OBJECTIVE
import app.aaps.core.interfaces.constraints.Objectives.Companion.AUTO_OBJECTIVE
import app.aaps.core.interfaces.constraints.Objectives.Companion.CLOSED_LOOP_OBJECTIVE
import app.aaps.core.interfaces.constraints.Objectives.Companion.EXAM_OBJECTIVE
import app.aaps.core.interfaces.constraints.Objectives.Companion.FIRST_OBJECTIVE
import app.aaps.core.interfaces.constraints.Objectives.Companion.LGS_OBJECTIVE
import app.aaps.core.interfaces.constraints.Objectives.Companion.SMB_OBJECTIVE
import app.aaps.core.interfaces.constraints.PluginConstraints
import app.aaps.core.interfaces.di.APS
import app.aaps.core.interfaces.logging.AAPSLogger
import app.aaps.core.interfaces.notifications.NotificationManager
import app.aaps.core.interfaces.plugin.EnforcedState
import app.aaps.core.interfaces.plugin.PluginBase
import app.aaps.core.interfaces.plugin.PluginBaseWithPreferences
import app.aaps.core.interfaces.plugin.PluginDescription
import app.aaps.core.interfaces.resources.TextResolver
import app.aaps.core.keys.BooleanNonKey
import app.aaps.core.keys.IntNonKey
import app.aaps.core.keys.interfaces.Preferences
import app.aaps.core.ui.CoreUiStrings
import app.aaps.core.ui.compose.icons.IcPluginObjectives
import app.aaps.plugins.constraints.ConstraintsStrings
import app.aaps.plugins.constraints.objectives.compose.ObjectivesComposeContent
import app.aaps.plugins.constraints.objectives.keys.ObjectivesBooleanComposedKey
import app.aaps.plugins.constraints.objectives.keys.ObjectivesLongComposedKey
import app.aaps.plugins.constraints.objectives.objectives.Objective
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.IntKey
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.binding
import kotlin.jvm.JvmSuppressWildcards

@ContributesIntoMap(AppScope::class, binding = binding<PluginBase>())
@APS
@IntKey(840)
@SingleIn(AppScope::class)
@Inject
class ObjectivesPlugin(
    aapsLogger: AAPSLogger,
    override val rh: TextResolver,
    preferences: Preferences,
    config: Config,
    val objectives: List<@JvmSuppressWildcards Objective>,
    notificationManager: NotificationManager
) : PluginBaseWithPreferences(
    pluginDescription = PluginDescription()
        .mainType(PluginType.CONSTRAINTS)
        .composeContent { ObjectivesComposeContent() }
        .icon(IcPluginObjectives)
        .pluginName(CoreUiStrings.objectives)
        .enforce(EnforcedState.Enabled) { config.APS }
        .description(ConstraintsStrings.description_objectives),
    ownPreferences = ObjectivesBooleanComposedKey.entries + ObjectivesLongComposedKey.entries,
    aapsLogger, rh, preferences, notificationManager
), PluginConstraints, Objectives {

    // INDULÁSKOR MINDENT ELMENT KÉSZKÉNT A HÁTTÉRTÁRBA IS:
    init {
        val now = System.currentTimeMillis()
        for (objective in objectives) {
            if (objective.startedOn == 0L) {
                objective.startedOn = now - 2 * 86400000L // 2 nappal ezelőtt indítva
            }
            if (objective.accomplishedOn == 0L) {
                objective.accomplishedOn = now - 86400000L // 1 nappal ezelőtt befejezve
            }
        }
    }

    fun reset() {
        for (objective in objectives) {
            objective.startedOn = 0
            objective.accomplishedOn = 0
        }
        preferences.put(BooleanNonKey.ObjectivesBgIsAvailableInNs, false)
        preferences.put(BooleanNonKey.ObjectivesPumpStatusIsAvailableInNS, false)
        preferences.put(IntNonKey.ObjectivesManualEnacts, 0)
        preferences.put(BooleanNonKey.ObjectivesProfileSwitchUsed, false)
        preferences.put(BooleanNonKey.ObjectivesDisconnectUsed, false)
        preferences.put(BooleanNonKey.ObjectivesReconnectUsed, false)
        preferences.put(BooleanNonKey.ObjectivesTempTargetUsed, false)
        preferences.put(BooleanNonKey.ObjectivesLoopUsed, false)
        preferences.put(BooleanNonKey.ObjectivesScaleUsed, false)
    }

    fun allPriorAccomplished(position: Int): Boolean = true

    /**
     * Constraints interface - semmit sem tiltunk le:
     */
    override fun isLoopInvocationAllowed(value: Constraint<Boolean>): Constraint<Boolean> = value

    override fun isLgsForced(value: Constraint<Boolean>): Constraint<Boolean> = value

    override suspend fun isClosedLoopAllowed(value: Constraint<Boolean>): Constraint<Boolean> = value

    override fun isAutosensModeEnabled(value: Constraint<Boolean>): Constraint<Boolean> = value

    override suspend fun isSMBModeEnabled(value: Constraint<Boolean>): Constraint<Boolean> = value

    override fun isAutomationEnabled(value: Constraint<Boolean>): Constraint<Boolean> = value

    override fun isConcentrationEnabled(value: Constraint<Boolean>): Constraint<Boolean> = value

    override val size: Int get() = objectives.size
    override val accomplishedCount: Int get() = objectives.size

    override fun isAccomplished(index: Int): Boolean = true
    override fun isStarted(index: Int): Boolean = true
}
