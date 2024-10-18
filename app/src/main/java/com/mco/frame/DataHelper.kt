package com.mco.frame

import java.util.ArrayList

object DataHelper {
    fun loadTweetData(): ArrayList<AddedNotesModel> {
        val data = ArrayList<AddedNotesModel>()
        data.add(
            AddedNotesModel(
                "armin.armode.armedian", "Armin Arlert", R.drawable.armin
            )
        )
        data.add(
            AddedNotesModel(
                "wonderboy", "Zeke Jaeger",
                R.drawable.zeke
            )
        )
        data.add(
            AddedNotesModel(
                "eldian.pride", "Falco Grice",
                R.drawable.falco
            )
        )
        data.add(
            AddedNotesModel(
                "rudolph_the_reiner", "Reiner Braun",
                R.drawable.reiner
            )
        )
        data.add(
            AddedNotesModel(
                "jaegermeister", "Eren Jaeger",
                R.drawable.eren
            )
        )

        return data
    }
}