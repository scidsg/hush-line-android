package org.scidsg.hushline.android

sealed class UIState {

    data class Starting(
        private val torPercent: Int
    ) : UIState() {
        init {
            require(torPercent in 0..100)
        }
    }

    data class Started(
        val onionAddress: String
    ) : UIState()

    data class Stopping(
        val onionAddress: String
    ) : UIState()

    object Stopped : UIState()

    data class Error(
        val torFailedToConnect: Boolean = false,
    ) : UIState()
}