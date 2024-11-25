package com.mco.accessability

import com.mco.accessability.models.ReviewModel
import java.util.ArrayList

object DataHelper {
    fun loadTweetData(): ArrayList<ReviewModel> {
        val data = ArrayList<ReviewModel>()
        data.add(
            ReviewModel(
                "1","armin.armode.armedian", "Armin Arlert", R.drawable.armin, 5
            )
        )
        data.add(
            ReviewModel(
                "2","wonderboy", "Zeke Jaeger",
                R.drawable.zeke, 1
            )
        )
        data.add(
            ReviewModel(
                "3","eldian.pride", "Falco Grice",
                R.drawable.falco, 3
            )
        )
        data.add(
            ReviewModel(
                "4","rudolph_the_reiner", "Reiner Braun",
                R.drawable.reiner, 5
            )
        )
        data.add(
            ReviewModel(
                "5","jaegermeister", "Eren Jaeger",
                R.drawable.eren, 5
            )
        )

        return data
    }
}