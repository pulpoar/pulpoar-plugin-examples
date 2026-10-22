package com.pulpoar.tryon.example

import android.app.Application
import com.pulpoar.tryon.PulpoAnalyticsConsent
import com.pulpoar.tryon.PulpoTryOn
import com.pulpoar.tryon.PulpoTryOnConfiguration

class ExampleApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Once, before any try-on is shown. Nothing is reported while consent is pending: set
        // `PulpoTryOn.analyticsConsent` when the user answers your consent prompt (here, the switch in the Cart tab).
        PulpoTryOn.configure(this, PulpoTryOnConfiguration(analyticsConsent = PulpoAnalyticsConsent.PENDING))
    }
}
