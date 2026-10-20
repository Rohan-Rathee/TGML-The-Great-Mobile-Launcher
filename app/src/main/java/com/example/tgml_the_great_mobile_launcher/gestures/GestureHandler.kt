package com.example.tgml_the_great_mobile_launcher.gestures

class GestureHandler (
	private val mappings: List<GestureMapping>
){
	fun resolve( gesture: GestureType, context: GestureContext): GestureAction?
	{
		return mappings.find { it.gesture == gesture && it.context == context }?.action
	}
}