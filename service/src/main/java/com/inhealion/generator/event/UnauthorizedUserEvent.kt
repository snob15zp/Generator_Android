package com.inhealion.generator.event

interface UnauthorizedUserEvent : EventDelegate<Unit>


internal class UnauthorizedUserEventImpl : UnauthorizedUserEvent, FlowEventDelegate<Unit>()
