package com.alt.otherlives.core.data

import com.alt.otherlives.core.model.Scenario
import com.alt.otherlives.core.model.TimelineChapter

object ScenarioCatalog {
    val scenarios = ScenarioCatalogValidation.validate(
        listOf(
        Scenario(
            id = "wealth",
            title = "What if I became wealthy?",
            subtitle = "A decade where one decision compounds.",
            chapters = listOf(
                TimelineChapter("2026", "You stop optimizing for comfort and start optimizing for leverage."),
                TimelineChapter("2028", "A small project becomes your first serious source of freedom."),
                TimelineChapter("2031", "You build systems that keep working while you sleep."),
                TimelineChapter("2034", "Money stops being the destination and becomes infrastructure."),
                TimelineChapter("2036", "Your life looks expensive from the outside — calm from the inside.")
            )
        ),
        Scenario(
            id = "japan",
            title = "What if I moved to Japan?",
            subtitle = "A new city, language and version of you.",
            chapters = listOf(
                TimelineChapter("2026", "You arrive with two bags and no familiar routine."),
                TimelineChapter("2027", "The city stops feeling foreign and starts feeling precise."),
                TimelineChapter("2029", "Your work, friends and habits become part of the place."),
                TimelineChapter("2032", "You realize you no longer translate your life in your head."),
                TimelineChapter("2036", "Home is no longer where you started.")
            )
        ),
        Scenario(
            id = "future",
            title = "What if I lived in 2100?",
            subtitle = "Your alternate life in a world that moved on.",
            chapters = listOf(
                TimelineChapter("2068", "Your identity becomes portable across physical and digital spaces."),
                TimelineChapter("2077", "Work is mostly direction, taste and judgment."),
                TimelineChapter("2086", "Cities feel quieter because infrastructure has become invisible."),
                TimelineChapter("2094", "Your oldest memories exist in forms younger generations can walk through."),
                TimelineChapter("2100", "The future feels ordinary when you live inside it.")
            )
        ),
        Scenario(
            id = "disappear",
            title = "What if I disappeared for 5 years?",
            subtitle = "No audience. No updates. Just change.",
            chapters = listOf(
                TimelineChapter("Year 1", "You remove the noise before you know what will replace it."),
                TimelineChapter("Year 2", "The new routine stops feeling temporary."),
                TimelineChapter("Year 3", "Your skills become visible before you do."),
                TimelineChapter("Year 4", "You stop measuring progress through other people."),
                TimelineChapter("Year 5", "You return with a life that no longer needs explaining.")
            )
        ),
        Scenario(
            id = "famous",
            title = "What if I became famous?",
            subtitle = "The version where everyone learns your name.",
            chapters = listOf(
                TimelineChapter("2026", "One piece of your work escapes your usual circle."),
                TimelineChapter("2027", "Recognition arrives faster than your routines can adapt."),
                TimelineChapter("2029", "You learn the difference between being visible and being known."),
                TimelineChapter("2032", "You build boundaries around the parts of life that still belong only to you."),
                TimelineChapter("2036", "Fame becomes background noise; the work becomes the point again.")
            )
        ),
        Scenario(
            id = "mars",
            title = "What if I lived on Mars?",
            subtitle = "A life measured in launch windows and red horizons.",
            chapters = listOf(
                TimelineChapter("2037", "Earth becomes a blue memory framed by a small window."),
                TimelineChapter("2038", "Every ordinary habit becomes part of keeping a settlement alive."),
                TimelineChapter("2041", "You stop thinking of the habitat as temporary."),
                TimelineChapter("2045", "A generation arrives that has never felt rain."),
                TimelineChapter("2050", "You watch two worlds in the sky and call one of them home.")
            )
        ),
        Scenario(
            id = "artist",
            title = "What if I became an artist?",
            subtitle = "A life built around making things that did not exist before.",
            chapters = listOf(
                TimelineChapter("2026", "You make something every day before deciding whether it is good."),
                TimelineChapter("2028", "Your style appears slowly, mostly through the choices you keep repeating."),
                TimelineChapter("2030", "Someone recognizes your work before seeing your name."),
                TimelineChapter("2033", "You stop waiting for inspiration and build a practice instead."),
                TimelineChapter("2036", "Your archive becomes a map of who you were becoming.")
            )
        ),
        Scenario(
            id = "restart",
            title = "What if I started over?",
            subtitle = "Same memories. Different decisions.",
            chapters = listOf(
                TimelineChapter("Day 1", "You keep the lessons and remove the obligations that no longer fit."),
                TimelineChapter("Month 6", "The empty space begins filling with deliberate choices."),
                TimelineChapter("Year 2", "Your new life stops feeling like an escape."),
                TimelineChapter("Year 5", "The decisions that once felt radical now look obvious."),
                TimelineChapter("Year 10", "You barely recognize the path you once thought was fixed.")
            )
        )
        )
    )

    private val frenchScenarios = ScenarioCatalogValidation.validate(
        listOf(
            Scenario(
                id = "wealth",
                title = "Et si je devenais riche ?",
                subtitle = "Une décennie où une seule décision change tout.",
                chapters = listOf(
                    TimelineChapter("2026", "Tu arrêtes d’optimiser le confort et commences à construire des leviers."),
                    TimelineChapter("2028", "Un petit projet devient ta première véritable source de liberté."),
                    TimelineChapter("2031", "Tu construis des systèmes qui continuent de fonctionner pendant ton sommeil."),
                    TimelineChapter("2034", "L’argent cesse d’être une destination et devient une infrastructure."),
                    TimelineChapter("2036", "De l’extérieur, ta vie paraît luxueuse ; de l’intérieur, elle est calme.")
                )
            ),
            Scenario(
                id = "japan",
                title = "Et si je partais vivre au Japon ?",
                subtitle = "Une nouvelle ville, une nouvelle langue, une nouvelle version de toi.",
                chapters = listOf(
                    TimelineChapter("2026", "Tu arrives avec deux valises et aucune routine familière."),
                    TimelineChapter("2027", "La ville cesse de te sembler étrangère et commence à te sembler précise."),
                    TimelineChapter("2029", "Ton travail, tes amis et tes habitudes font désormais partie du lieu."),
                    TimelineChapter("2032", "Tu réalises que tu ne traduis plus ta vie dans ta tête."),
                    TimelineChapter("2036", "Chez toi n’est plus l’endroit d’où tu es parti.")
                )
            ),
            Scenario(
                id = "future",
                title = "Et si je vivais en 2100 ?",
                subtitle = "Ta vie alternative dans un monde qui a continué d’avancer.",
                chapters = listOf(
                    TimelineChapter("2068", "Ton identité devient portable entre les espaces physiques et numériques."),
                    TimelineChapter("2077", "Le travail repose surtout sur la direction, le goût et le jugement."),
                    TimelineChapter("2086", "Les villes semblent plus calmes parce que l’infrastructure est devenue invisible."),
                    TimelineChapter("2094", "Tes plus vieux souvenirs existent sous des formes que les nouvelles générations peuvent parcourir."),
                    TimelineChapter("2100", "Le futur paraît ordinaire lorsqu’on vit à l’intérieur.")
                )
            ),
            Scenario(
                id = "disappear",
                title = "Et si je disparaissais pendant 5 ans ?",
                subtitle = "Pas de public. Pas de nouvelles. Seulement le changement.",
                chapters = listOf(
                    TimelineChapter("Année 1", "Tu supprimes le bruit avant même de savoir ce qui le remplacera."),
                    TimelineChapter("Année 2", "La nouvelle routine cesse de sembler temporaire."),
                    TimelineChapter("Année 3", "Tes compétences deviennent visibles avant toi."),
                    TimelineChapter("Année 4", "Tu arrêtes de mesurer tes progrès à travers les autres."),
                    TimelineChapter("Année 5", "Tu reviens avec une vie qui n’a plus besoin d’être expliquée.")
                )
            ),
            Scenario(
                id = "famous",
                title = "Et si je devenais célèbre ?",
                subtitle = "La version où tout le monde finit par connaître ton nom.",
                chapters = listOf(
                    TimelineChapter("2026", "Une de tes créations dépasse soudain ton cercle habituel."),
                    TimelineChapter("2027", "La reconnaissance arrive plus vite que tes habitudes ne peuvent s’adapter."),
                    TimelineChapter("2029", "Tu apprends la différence entre être visible et être réellement connu."),
                    TimelineChapter("2032", "Tu poses des limites autour des parties de ta vie qui n’appartiennent encore qu’à toi."),
                    TimelineChapter("2036", "La célébrité devient un bruit de fond ; le travail redevient l’essentiel.")
                )
            ),
            Scenario(
                id = "mars",
                title = "Et si je vivais sur Mars ?",
                subtitle = "Une vie rythmée par les fenêtres de lancement et les horizons rouges.",
                chapters = listOf(
                    TimelineChapter("2037", "La Terre devient un souvenir bleu encadré par une petite fenêtre."),
                    TimelineChapter("2038", "Chaque habitude ordinaire participe désormais à maintenir une colonie en vie."),
                    TimelineChapter("2041", "Tu cesses de considérer l’habitat comme temporaire."),
                    TimelineChapter("2045", "Une génération apparaît sans avoir jamais senti la pluie."),
                    TimelineChapter("2050", "Tu observes deux mondes dans le ciel et appelles l’un d’eux chez toi.")
                )
            ),
            Scenario(
                id = "artist",
                title = "Et si je devenais artiste ?",
                subtitle = "Une vie construite autour de choses qui n’existaient pas auparavant.",
                chapters = listOf(
                    TimelineChapter("2026", "Tu crées quelque chose chaque jour avant même de décider si c’est réussi."),
                    TimelineChapter("2028", "Ton style apparaît lentement, surtout à travers les choix que tu répètes."),
                    TimelineChapter("2030", "Quelqu’un reconnaît ton travail avant même de voir ton nom."),
                    TimelineChapter("2033", "Tu arrêtes d’attendre l’inspiration et construis une pratique à la place."),
                    TimelineChapter("2036", "Tes archives deviennent une carte de la personne que tu étais en train de devenir.")
                )
            ),
            Scenario(
                id = "restart",
                title = "Et si je recommençais tout ?",
                subtitle = "Les mêmes souvenirs. Des décisions différentes.",
                chapters = listOf(
                    TimelineChapter("Jour 1", "Tu gardes les leçons et retires les obligations qui ne te correspondent plus."),
                    TimelineChapter("Mois 6", "L’espace vide commence à se remplir de choix délibérés."),
                    TimelineChapter("Année 2", "Ta nouvelle vie cesse de ressembler à une fuite."),
                    TimelineChapter("Année 5", "Les décisions qui semblaient autrefois radicales paraissent maintenant évidentes."),
                    TimelineChapter("Année 10", "Tu reconnais à peine le chemin que tu pensais autrefois immuable.")
                )
            )
        )
    )

    fun forLanguage(language: String): List<Scenario> =
        if (language.lowercase().startsWith("fr")) frenchScenarios else scenarios

}
