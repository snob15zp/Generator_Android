package com.inhealion.generator.events

import com.inhealion.generator.event.EventDelegate
import com.inhealion.generator.event.FlowEventDelegate
import com.inhealion.generator.service.ImportState

interface ImportStateEventDelegate : EventDelegate<ImportState>

internal class ImportStateEventDelegateImpl : ImportStateEventDelegate, FlowEventDelegate<ImportState>()
