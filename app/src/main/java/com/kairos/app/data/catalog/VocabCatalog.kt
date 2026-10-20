package com.kairos.app.data.catalog

/**
 * The word garden.
 *
 * Every entry is a word you could actually use this week: no filler, no "sesquipedalian".
 * Definitions are written in plain English; the etymology is the reason the word means what
 * it means, which is the part people remember.
 */
data class Word(
    val id: String,
    val term: String,
    val ipa: String,
    val partOfSpeech: String,
    val definition: String,
    val example: String,
    val etymology: String,
    val synonyms: List<String> = emptyList(),
    val themes: List<String> = emptyList(),
    val family: String = "",
)

object VocabCatalog {

    const val MIND = "mind"
    const val LANGUAGE = "language"
    const val NATURE = "nature"
    const val CRAFT = "craft"
    const val PEOPLE = "people"
    const val TIME = "time"
    const val FEELINGS = "feelings"
    const val IDEAS = "ideas"

    val themes = listOf(
        MIND to "Mind & philosophy",
        LANGUAGE to "Words & language",
        NATURE to "Nature & science",
        CRAFT to "Art & making",
        PEOPLE to "People & stories",
        TIME to "Time & memory",
        FEELINGS to "Feelings",
        IDEAS to "Big ideas",
    )

    val all: List<Word> = listOf(
        // ---------------------------------------------------------------- mind
        Word("wd-001", "lucid", "/ˈluː.sɪd/", "adjective", "Clear and easy to follow; thinking with the lights on.", "Her explanation was so lucid that the room went quiet.", "From Latin lux, light — the same root as translucent.", listOf("clear", "coherent", "limpid"), listOf(MIND, IDEAS), "lucidity"),
        Word("wd-002", "equanimity", "/ˌek.wəˈnɪm.ə.ti/", "noun", "Calmness of mind, especially when things go badly.", "He met the news with an equanimity that unsettled everyone.", "Latin aequus (even) + animus (mind): an even mind.", listOf("composure", "poise"), listOf(MIND, FEELINGS), "equanimous"),
        Word("wd-003", "ruminate", "/ˈruː.mɪ.neɪt/", "verb", "To turn a thought over and over, the way a cow chews grass.", "She ruminated on the email for an hour and then didn't send it.", "Latin ruminare, to chew the cud. Thinking, at its most bovine.", listOf("mull", "brood"), listOf(MIND), "rumination"),
        Word("wd-004", "apophenia", "/ˌæp.əˈfiː.ni.ə/", "noun", "Seeing meaningful patterns in random noise.", "The constellation is apophenia with a good publicist.", "Coined in 1958 from Greek apo (away) + phainein (to show).", listOf("pattern-seeking"), listOf(MIND, IDEAS), "apophenic"),
        Word("wd-005", "sonder", "/ˈsɒn.də/", "noun", "The sudden realisation that every stranger has a life as vivid as your own.", "Standing on the platform, sonder hit him all at once.", "Coined by John Koenig for The Dictionary of Obscure Sorrows.", listOf("recognition"), listOf(FEELINGS, PEOPLE), ""),
        Word("wd-006", "ataraxia", "/ˌæt.əˈræk.si.ə/", "noun", "A untroubled quiet of the mind, sought by the Epicureans.", "His ataraxia was not indifference; it was practice.", "Greek a- (not) + tarassein (to disturb).", listOf("serenity"), listOf(MIND, FEELINGS), "ataractic"),
        Word("wd-007", "metacognition", "/ˌmet.ə.kɒɡˈnɪʃ.ən/", "noun", "Thinking about how you think.", "The habit of metacognition turned her bad days into data.", "meta (beyond) + Latin cognitio (knowing).", listOf("self-awareness"), listOf(MIND, IDEAS), "metacognitive"),
        Word("wd-008", "ambivalence", "/æmˈbɪv.ə.ləns/", "noun", "Holding two opposite feelings about the same thing at once.", "Her ambivalence about leaving was not indecision; it was honesty.", "Latin ambo (both) + valentia (strength).", listOf("mixed feelings"), listOf(FEELINGS, MIND), "ambivalent"),
        Word("wd-009", "sublimate", "/ˈsʌb.lɪ.meɪt/", "verb", "To turn a raw impulse into something finer.", "He sublimated the anger into a very tidy garage.", "Latin sublimare, to lift up. Chemistry borrowed it later.", listOf("channel", "refine"), listOf(MIND, CRAFT), "sublimation"),
        Word("wd-010", "presence", "/ˈprez.əns/", "noun", "The quality of being fully here, not half somewhere else.", "She had a presence that made phones go face-down.", "Latin prae (before) + esse (to be): to be before someone.", listOf("attentiveness"), listOf(MIND, FEELINGS), "present"),

        // ---------------------------------------------------------------- language
        Word("wd-011", "palimpsest", "/ˈpæl.ɪmp.sest/", "noun", "A surface written on, scraped clean, and written on again.", "The city is a palimpsest of every plan that failed.", "Greek palin (again) + psēstos (scraped).", listOf("layered", "overwritten"), listOf(LANGUAGE, TIME), "palimpsestic"),
        Word("wd-012", "lacuna", "/ləˈkjuː.nə/", "noun", "A gap where something should be.", "There is a lacuna in the record exactly where the argument happened.", "Latin lacuna, a ditch or pool.", listOf("gap", "omission"), listOf(LANGUAGE, IDEAS), "lacunae"),
        Word("wd-013", "ineffable", "/ɪnˈef.ə.bəl/", "adjective", "Too large or fine for words.", "The ineffable part is the part you actually remember.", "Latin in- (not) + effabilis (speakable).", listOf("indescribable"), listOf(LANGUAGE, FEELINGS), "ineffability"),
        Word("wd-014", "aphorism", "/ˈæf.ər.ɪ.zəm/", "noun", "A short, sharp sentence that carries a whole argument.", "The best aphorisms are arrows with the fletching still on.", "Greek aphorismos, a definition or boundary.", listOf("maxim", "epigram"), listOf(LANGUAGE, IDEAS), "aphoristic"),
        Word("wd-015", "elegy", "/ˈel.ə.dʒi/", "noun", "A poem or song of grief, usually for someone gone.", "He wrote an elegy for the version of himself that used to be brave.", "Greek elegos, a song of mourning.", listOf("lament", "requiem"), listOf(LANGUAGE, FEELINGS), "elegiac"),
        Word("wd-016", "vernacular", "/vəˈnæk.jə.lər/", "noun", "The language people actually speak when no one official is listening.", "The vernacular of the group chat is its own dialect.", "Latin vernaculus, native to the household.", listOf("dialect", "idiom"), listOf(LANGUAGE, PEOPLE), "vernacularly"),
        Word("wd-017", "tmesis", "/ˈtmiː.sɪs/", "noun", "Splitting a word with another word — abso-blooming-lutely.", "Tmesis is how English swears with architecture.", "Greek tmēsis, a cutting.", listOf("infixion"), listOf(LANGUAGE), "tmesic"),
        Word("wd-018", "cognate", "/ˈkɒɡ.neɪt/", "adjective", "Related by descent, said of words and ideas.", "Night and nicht are cognates; so are two people's fears.", "Latin co- (together) + natus (born).", listOf("kindred"), listOf(LANGUAGE), "cognation"),
        Word("wd-019", "reticent", "/ˈret.ɪ.sənt/", "adjective", "Reserved; saying less than you know.", "He was reticent about the promotion, which told everyone everything.", "Latin reticere, to keep silent.", listOf("reserved", "tight-lipped"), listOf(LANGUAGE, PEOPLE), "reticence"),
        Word("wd-020", "neologism", "/niˈɒl.ə.dʒɪ.zəm/", "noun", "A newly minted word, often still warm.", "Every generation mistakes its neologisms for the end of language.", "Greek neo- (new) + logos (word).", listOf("coinage"), listOf(LANGUAGE, IDEAS), "neological"),

        // ---------------------------------------------------------------- nature
        Word("wd-021", "petrichor", "/ˈpet.rɪ.kɔːr/", "noun", "The smell of rain landing on dry earth.", "Petrichor came through the window before the storm did.", "Greek petra (stone) + ichor, the blood of the gods.", listOf("rain-smell"), listOf(NATURE, FEELINGS), "petrichorous"),
        Word("wd-022", "effervescent", "/ˌef.əˈves.ənt/", "adjective", "Bubbling; lively enough to lift a room.", "Her effervescent laugh made the dentist's waiting room bearable.", "Latin effervescere, to boil up.", listOf("fizzy", "buoyant"), listOf(NATURE, FEELINGS), "effervescence"),
        Word("wd-023", "dappled", "/ˈdæp.əld/", "adjective", "Marked with soft patches of light and shade.", "He read in the dappled shade until the page went grey.", "Perhaps from Old Norse depill, a spot.", listOf("mottled", "flecked"), listOf(NATURE, CRAFT), "dapple"),
        Word("wd-024", "gossamer", "/ˈɡɒs.ə.mər/", "noun", "Something impossibly fine and light, like spider silk in autumn.", "A gossamer of fog hung over the field.", "From goose + summer: the time of year when cobwebs drift.", listOf("filmy", "delicate"), listOf(NATURE, CRAFT), "gossamery"),
        Word("wd-025", "susurrus", "/suːˈsʌr.əs/", "noun", "A whispering, murmuring sound.", "The susurrus of the library was the sound of everyone thinking.", "Latin susurrus, a whisper.", listOf("murmur", "rustle"), listOf(NATURE, LANGUAGE), "susurrant"),
        Word("wd-026", "bioluminescence", "/ˌbaɪ.əʊ.luː.mɪˈnes.əns/", "noun", "Light made by living things.", "The bay glowed with bioluminescence, as if the water had opinions.", "bio (life) + Latin lumen (light).", listOf("living light"), listOf(NATURE), "bioluminescent"),
        Word("wd-027", "fecund", "/ˈfek.ənd/", "adjective", "Producing abundantly; full of life and ideas.", "It was a fecund year — three books, two babies, one garden.", "Latin fecundus, fruitful.", listOf("fertile", "prolific"), listOf(NATURE, IDEAS), "fecundity"),
        Word("wd-028", "verdant", "/ˈvɜː.dənt/", "adjective", "Green with new growth.", "The hills went verdant overnight and startled everyone.", "Old French vert, green.", listOf("lush", "leafy"), listOf(NATURE), "verdure"),
        Word("wd-029", "diurnal", "/daɪˈɜː.nəl/", "adjective", "Of the day; awake when the sun is.", "His diurnal rhythm had collapsed into a 3 a.m. hobby.", "Latin dies, day.", listOf("daytime"), listOf(NATURE, TIME), "diurnally"),
        Word("wd-030", "halcyon", "/ˈhæl.si.ən/", "adjective", "Idyllically calm, usually describing a past summer.", "The halcyon days were, in fact, mostly homework and heat rash.", "Greek halkyon, a kingfisher said to calm the sea.", listOf("golden", "serene"), listOf(NATURE, TIME), "halcyon"),

        // ---------------------------------------------------------------- craft
        Word("wd-031", "bricolage", "/ˌbriː.kəˈlɑːʒ/", "noun", "Making something good from whatever is lying around.", "Her flat was a bricolage of inherited chairs and good light.", "French bricoler, to tinker.", listOf("improvisation"), listOf(CRAFT, IDEAS), "bricoleur"),
        Word("wd-032", "patina", "/ˈpæt.ɪ.nə/", "noun", "The sheen that use and time leave on a surface.", "The brass had a patina that no shop could fake.", "Italian patina, a shallow dish; later, the film on bronze.", listOf("sheen", "wear"), listOf(CRAFT, TIME), "patinated"),
        Word("wd-033", "maquette", "/mæˈket/", "noun", "A small rough model made before the real thing.", "The maquette was cardboard and tape and better than the final building.", "French, from Italian macchietta, a sketch.", listOf("model", "mock-up"), listOf(CRAFT), "maquettes"),
        Word("wd-034", "draught", "/drɑːft/", "noun", "The plan of a drawing; also the pull of air through a room.", "He redrew the draught seven times and kept the first.", "Old English dragan, to draw.", listOf("sketch", "outline"), listOf(CRAFT, LANGUAGE), "draughtsman"),
        Word("wd-035", "burnish", "/ˈbɜː.nɪʃ/", "verb", "To polish by rubbing until it shines.", "She burnished the idea in conversation until it gleamed.", "Old French brunir, to make brown or bright.", listOf("polish", "buff"), listOf(CRAFT), "burnished"),
        Word("wd-036", "improvisation", "/ˌɪm.prə.vɪˈzeɪ.ʃən/", "noun", "Making it up as you go, but with skill.", "The best part of the set was pure improvisation.", "Latin in- (not) + providere (to foresee).", listOf("extemporising"), listOf(CRAFT, PEOPLE), "improvisation"),
        Word("wd-037", "chiaroscuro", "/kiˌɑː.rəˈskʊə.rəʊ/", "noun", "The dramatic use of light and shadow.", "The photograph's chiaroscuro did most of the emotional work.", "Italian chiaro (light) + oscuro (dark).", listOf("contrast"), listOf(CRAFT, IDEAS), "chiaroscural"),
        Word("wd-038", "assemblage", "/əˈsæm.blɪdʒ/", "noun", "A work made by putting found things together.", "The assemblage of ticket stubs was the most honest portrait of her year.", "French assembler, to join.", listOf("collage"), listOf(CRAFT), "assemble"),
        Word("wd-039", "heuristic", "/hjʊˈrɪs.tɪk/", "adjective", "A shortcut that usually works, not a guarantee.", "Rule of thumb is a heuristic with better branding.", "Greek heuriskein, to find.", listOf("rule of thumb"), listOf(IDEAS, CRAFT), "heuristically"),
        Word("wd-040", "artisan", "/ˈɑː.tɪ.zæn/", "noun", "Someone who makes things with their hands and their name on them.", "The bread was made by an artisan who refused to hurry.", "French, from Italian artigiano, a craftsman.", listOf("maker", "craftsperson"), listOf(CRAFT, PEOPLE), "artisanal"),

        // ---------------------------------------------------------------- people
        Word("wd-041", "compersion", "/kəmˈpɜː.ʒən/", "noun", "Joy taken in someone else's joy.", "Her compersion was genuine; she was first to the phone.", "Coined in the 1970s, perhaps from Latin com- + passio.", listOf("sympathetic joy"), listOf(PEOPLE, FEELINGS), "compersive"),
        Word("wd-042", "gravitas", "/ˈɡræv.ɪ.tæs/", "noun", "Weight and dignity of manner.", "He had the gravitas of someone who had already apologised once.", "Latin gravitas, weight.", listOf("dignity", "presence"), listOf(PEOPLE), "grave"),
        Word("wd-043", "repartee", "/ˌrep.ɑːˈtiː/", "noun", "Quick, witty conversation that volleys back and forth.", "Their repartee was the only thing keeping the dinner alive.", "French repartie, a rapid reply.", listOf("banter"), listOf(PEOPLE, LANGUAGE), "repartee"),
        Word("wd-044", "confidant", "/ˈkɒn.fɪ.dænt/", "noun", "Someone you tell the true version to.", "A confidant is cheaper than a therapist and harder to find.", "French confident, from Latin confidere, to trust.", listOf("trusted friend"), listOf(PEOPLE, FEELINGS), "confidante"),
        Word("wd-045", "gregarious", "/ɡrɪˈɡeə.ri.əs/", "adjective", "Fond of company; energised by a crowd.", "He was gregarious in a way that made solitude look like a rumour.", "Latin grex, a flock.", listOf("sociable", "outgoing"), listOf(PEOPLE), "gregariousness"),
        Word("wd-046", "solipsism", "/ˈsɒl.ɪp.sɪ.zəm/", "noun", "The belief that only your own mind is real.", "Social media is solipsism with a follower count.", "Latin solus (alone) + ipse (self).", listOf("self-absorption"), listOf(IDEAS, MIND), "solipsistic"),
        Word("wd-047", "mensch", "/mentʃ/", "noun", "A person of integrity and honour; a stand-up human.", "He returned the wallet with the cash still in it. A mensch.", "Yiddish, from German Mensch, a human being.", listOf("decent person"), listOf(PEOPLE), "menschlich"),
        Word("wd-048", "attunement", "/əˈtjuːn.mənt/", "noun", "The state of being in tune with another person.", "Good friendship is mostly attunement and snacks.", "From tune, a sixteenth-century musical term.", listOf("harmony", "rapport"), listOf(PEOPLE, FEELINGS), "attuned"),
        Word("wd-049", "charisma", "/kəˈrɪz.mə/", "noun", "The pull a person has on a room, hard to name and easy to feel.", "His charisma was mostly the fact that he remembered your name.", "Greek kharisma, a gift of grace.", listOf("magnetism", "presence"), listOf(PEOPLE), "charismatic"),
        Word("wd-050", "kith", "/kɪθ/", "noun", "The people you know and belong to; kin and acquaintance together.", "The wedding gathered all her kith into one hot hall.", "Old English cȳth, knowledge or acquaintance.", listOf("kin", "circle"), listOf(PEOPLE, TIME), "kith and kin"),

        // ---------------------------------------------------------------- time
        Word("wd-051", "anachronism", "/əˈnæk.rə.nɪ.zəm/", "noun", "Something out of its proper time.", "A pocket watch in a TikTok is a charming anachronism.", "Greek ana (against) + khronos (time).", listOf("throwback"), listOf(TIME, IDEAS), "anachronistic"),
        Word("wd-052", "ephemeral", "/ɪˈfem.ər.əl/", "adjective", "Lasting a very short time.", "The ephemeral joy of a good hair day.", "Greek ephemeros, lasting only a day.", listOf("fleeting", "transient"), listOf(TIME, NATURE), "ephemera"),
        Word("wd-053", "nostalgia", "/nɒˈstæl.dʒə/", "noun", "A tender ache for a time you cannot return to.", "Nostalgia is memory with the arguments edited out.", "Greek nostos (homecoming) + algos (pain).", listOf("longing"), listOf(TIME, FEELINGS), "nostalgic"),
        Word("wd-054", "retrospect", "/ˈret.rə.spekt/", "noun", "Looking back at what has already happened.", "In retrospect, the warning signs had been wearing a hat.", "Latin retro (back) + specere (to look).", listOf("hindsight"), listOf(TIME, MIND), "retrospective"),
        Word("wd-055", "perennial", "/pəˈren.i.əl/", "adjective", "Lasting or recurring year after year.", "The perennial question at family dinners: so, any plans?", "Latin per (through) + annus (year).", listOf("enduring"), listOf(TIME, NATURE), "perennially"),
        Word("wd-056", "interstice", "/ɪnˈtɜː.stɪs/", "noun", "A small gap between things.", "The best conversations happen in the interstices of the schedule.", "Latin inter (between) + sistere (to stand).", listOf("gap", "crack"), listOf(TIME, IDEAS), "interstitial"),
        Word("wd-057", "antediluvian", "/ˌæn.ti.dɪˈluː.vi.ən/", "adjective", "Extremely old-fashioned; from before the flood.", "The office printer was antediluvian and proud of it.", "Latin ante (before) + diluvium (flood).", listOf("archaic", "ancient"), listOf(TIME, LANGUAGE), "antediluvially"),
        Word("wd-058", "incipit", "/ˈɪn.kɪ.pɪt/", "noun", "The opening words of a text.", "The incipit was so good the rest of the book never recovered.", "Latin, it begins.", listOf("opening"), listOf(TIME, LANGUAGE), "incipient"),
        Word("wd-059", "moratorium", "/ˌmɒr.əˈtɔː.ri.əm/", "noun", "An agreed pause on an activity.", "They called a moratorium on new projects until the old ones worked.", "Latin morari, to delay.", listOf("pause", "freeze"), listOf(TIME, IDEAS), "moratory"),
        Word("wd-060", "punctual", "/ˈpʌŋk.tʃu.əl/", "adjective", "On time, in the way that respects other people's time.", "She was punctual not because she was anxious but because she was kind.", "Latin punctum, a point.", listOf("on time"), listOf(TIME, PEOPLE), "punctuality"),

        // ---------------------------------------------------------------- feelings
        Word("wd-061", "wistful", "/ˈwɪst.fəl/", "adjective", "A quiet, sweet sadness about something out of reach.", "He gave the old house a wistful look and drove on.", "From wistly, an old word for intently.", listOf("yearning", "pensive"), listOf(FEELINGS, TIME), "wistfulness"),
        Word("wd-062", "eudaemonia", "/ˌjuː.diːˈməʊ.ni.ə/", "noun", "A deep, flourishing happiness built on living well.", "Not a good mood, but a good life: eudaemonia.", "Greek eu (good) + daimon (spirit).", listOf("flourishing"), listOf(FEELINGS, MIND), "eudaemonic"),
        Word("wd-063", "kenopsia", "/kəˈnɒp.si.ə/", "noun", "The eerie emptiness of a place usually full of people.", "The school at 6 p.m. had a kenopsia that made him hurry.", "Coined by John Koenig; Greek kenos (empty) + opsis (sight).", listOf("emptiness"), listOf(FEELINGS, PEOPLE), ""),
        Word("wd-064", "fractious", "/ˈfræk.ʃəs/", "adjective", "Irritable and hard to please.", "The toddler was fractious; so, honestly, was the adult.", "From fraction, in the sense of a broken peace.", listOf("testy", "cranky"), listOf(FEELINGS, PEOPLE), "fractiousness"),
        Word("wd-065", "tender", "/ˈten.dər/", "adjective", "Soft, sore, and easily bruised — of meat, or of a person.", "He was tender about it in a way he'd never admit.", "Latin tener, soft.", listOf("gentle", "raw"), listOf(FEELINGS, PEOPLE), "tenderness"),
        Word("wd-066", "elated", "/iˈleɪ.tɪd/", "adjective", "Lifted up by good news.", "She was elated for about four hours, then made a list.", "Latin elatus, raised up.", listOf("overjoyed", "thrilled"), listOf(FEELINGS), "elation"),
        Word("wd-067", "melancholy", "/ˈmel.ən.kɒl.i/", "noun", "A pensive sadness that can be almost pleasant.", "Autumn melancholy suited him; he bought a brown coat about it.", "Greek melas (black) + kholē (bile).", listOf("sorrow", "gloom"), listOf(FEELINGS, TIME), "melancholic"),
        Word("wd-068", "resilient", "/rɪˈzɪl.i.ənt/", "adjective", "Able to spring back after being pressed.", "The fern was more resilient than the fence.", "Latin resilire, to leap back.", listOf("tough", "buoyant"), listOf(FEELINGS, CRAFT), "resilience"),
        Word("wd-069", "yearning", "/ˈjɜː.nɪŋ/", "noun", "A strong, tender longing.", "The yearning for a different life is often a yearning for a different pace.", "Old English giernan, to desire.", listOf("longing", "craving"), listOf(FEELINGS, TIME), "yearn"),
        Word("wd-070", "aplomb", "/əˈplɒm/", "noun", "Self-assurance under pressure.", "She handled the collapse of the cake with total aplomb.", "French à plomb, according to the plumb line.", listOf("poise", "composure"), listOf(FEELINGS, PEOPLE), "aplomb"),

        // ---------------------------------------------------------------- ideas
        Word("wd-071", "hegemony", "/hɪˈɡem.ə.ni/", "noun", "Dominance of one group or idea over the rest.", "The hegemony of the spreadsheet is total.", "Greek hēgemonia, leadership.", listOf("dominance"), listOf(IDEAS, PEOPLE), "hegemonic"),
        Word("wd-072", "dialectic", "/ˌdaɪ.əˈlek.tɪk/", "noun", "Truth reached by arguing both sides against each other.", "The dialectic worked; the meeting did not.", "Greek dialektikē, the art of conversation.", listOf("debate", "reasoning"), listOf(IDEAS, LANGUAGE), "dialectical"),
        Word("wd-073", "ontology", "/ɒnˈtɒl.ə.dʒi/", "noun", "The study of what exists, and in what categories.", "The ontology of a group chat is a genuine philosophical problem.", "Greek ontos (being) + logos (study).", listOf("metaphysics"), listOf(IDEAS, MIND), "ontological"),
        Word("wd-074", "syncretism", "/ˈsɪŋ.krə.tɪ.zəm/", "noun", "The blending of different beliefs into one.", "The festival was syncretism with a fireworks budget.", "Greek synkretismos, a union of Cretan cities.", listOf("fusion"), listOf(IDEAS, PEOPLE), "syncretic"),
        Word("wd-075", "teleology", "/ˌtel.iˈɒl.ə.dʒi/", "noun", "Explaining things by the purpose they serve.", "Teleology is tempting: everything looks designed from the inside.", "Greek telos, end or purpose.", listOf("purpose-driven"), listOf(IDEAS, MIND), "teleological"),
        Word("wd-076", "paradox", "/ˈpær.ə.dɒks/", "noun", "A statement that contradicts itself and still seems true.", "The paradox of choice is that more options can mean less joy.", "Greek para (beyond) + doxa (opinion).", listOf("contradiction"), listOf(IDEAS), "paradoxical"),
        Word("wd-077", "axiom", "/ˈæk.si.əm/", "noun", "A starting assumption taken as true.", "Every plan rests on an axiom nobody examined.", "Greek axios, worthy.", listOf("premise"), listOf(IDEAS, MIND), "axiomatic"),
        Word("wd-078", "entropy", "/ˈen.trə.pi/", "noun", "The tendency of everything toward disorder.", "Entropy is why the cables in the drawer are like that.", "Greek en (in) + tropē (a turning).", listOf("disorder"), listOf(IDEAS, NATURE), "entropic"),
        Word("wd-079", "gestalt", "/ɡəˈʃtɑːlt/", "noun", "A whole that is more than the sum of its parts.", "The gestalt of the room was 'someone's grandmother has taste'.", "German Gestalt, shape or form.", listOf("whole"), listOf(IDEAS, CRAFT), "gestaltic"),
        Word("wd-080", "empirical", "/ɪmˈpɪr.ɪ.kəl/", "adjective", "Based on observation rather than theory.", "The empirical evidence was that the cat preferred the expensive bed.", "Greek empeiria, experience.", listOf("observed"), listOf(IDEAS, NATURE), "empiricism"),

        // ---------------------------------------------------------------- extra flavour
        Word("wd-081", "liminal", "/ˈlɪm.ɪ.nəl/", "adjective", "Of the threshold; in-between.", "The airport at 4 a.m. is the most liminal place we built.", "Latin limen, threshold.", listOf("threshold", "transitional"), listOf(IDEAS, TIME), "liminality"),
        Word("wd-082", "palpable", "/ˈpæl.pə.bəl/", "adjective", "So strong you could touch it.", "The relief in the room was palpable.", "Latin palpare, to touch gently.", listOf("tangible"), listOf(FEELINGS), "palpably"),
        Word("wd-083", "cadence", "/ˈkeɪ.dəns/", "noun", "The rhythm of a voice, a walk, or a season.", "Her cadence slowed when she was about to say something true.", "Latin cadere, to fall.", listOf("rhythm"), listOf(LANGUAGE, CRAFT), "cadenced"),
        Word("wd-084", "vivid", "/ˈvɪv.ɪd/", "adjective", "Bright, clear, alive in the mind.", "The memory was more vivid than the actual day.", "Latin vividus, full of life.", listOf("striking", "graphic"), listOf(FEELINGS, CRAFT), "vividness"),
        Word("wd-085", "reverie", "/ˈrev.ər.i/", "noun", "A pleasant, drifting daydream.", "The meeting interrupted a very good reverie about lunch.", "French rêverie, from rêver, to dream.", listOf("daydream"), listOf(MIND, FEELINGS), "reverist"),
        Word("wd-086", "alacrity", "/əˈlæk.rə.ti/", "noun", "Brisk and cheerful readiness.", "She agreed with an alacrity that made him suspicious.", "Latin alacer, lively.", listOf("eagerness"), listOf(FEELINGS, PEOPLE), "alacritous"),
        Word("wd-087", "bucolic", "/bjuːˈkɒl.ɪk/", "adjective", "Charmingly rural, in a way that smells of hay.", "The bucolic weekend involved one cow and a lot of mud.", "Greek boukolos, a cowherd.", listOf("pastoral", "rural"), listOf(NATURE, CRAFT), "bucolically"),
        Word("wd-088", "epiphany", "/ɪˈpɪf.ə.ni/", "noun", "A sudden, clear understanding.", "The epiphany arrived in the shower, as they do.", "Greek epiphaneia, a manifestation.", listOf("insight", "revelation"), listOf(MIND, IDEAS), "epiphanic"),
        Word("wd-089", "sanguine", "/ˈsæŋ.ɡwɪn/", "adjective", "Cheerfully optimistic.", "He was sanguine about the deadline, which worried everyone.", "Latin sanguis, blood — once thought to cause cheer.", listOf("hopeful", "buoyant"), listOf(FEELINGS, MIND), "sanguinity"),
        Word("wd-090", "sonorous", "/ˈsɒn.ər.əs/", "adjective", "Deep and full in sound.", "The sonorous narrator could make a receipt sound like scripture.", "Latin sonorus, from sonor, sound.", listOf("resonant", "rich"), listOf(LANGUAGE, CRAFT), "sonority"),
        Word("wd-091", "ubiquitous", "/juːˈbɪk.wɪ.təs/", "adjective", "Present everywhere at once.", "The tote bag had become ubiquitous and slightly ironic.", "Latin ubique, everywhere.", listOf("omnipresent"), listOf(IDEAS, PEOPLE), "ubiquity"),
        Word("wd-092", "tacit", "/ˈtæs.ɪt/", "adjective", "Understood without being said.", "They had a tacit agreement not to discuss the wedding.", "Latin tacere, to be silent.", listOf("implicit", "unspoken"), listOf(LANGUAGE, PEOPLE), "tacitly"),
        Word("wd-093", "cusp", "/kʌsp/", "noun", "The point where one thing becomes another.", "On the cusp of thirty, he took up the guitar.", "Latin cuspis, a point.", listOf("brink", "threshold"), listOf(TIME, IDEAS), "cuspidal"),
        Word("wd-094", "foible", "/ˈfɔɪ.bəl/", "noun", "A small, harmless weakness of character.", "His foible was narrating films during films.", "French faible, weak.", listOf("quirk", "flaw"), listOf(PEOPLE, FEELINGS), "foibles"),
        Word("wd-095", "zeal", "/ziːl/", "noun", "Enthusiastic devotion to a cause.", "Her zeal for composting bordered on the evangelical.", "Greek zēlos, ardour.", listOf("passion", "fervour"), listOf(FEELINGS, IDEAS), "zealous"),
        Word("wd-096", "quiescent", "/kwiˈes.ənt/", "adjective", "Quiet, still, temporarily inactive.", "The garden was quiescent, gathering itself for spring.", "Latin quiescere, to rest.", listOf("dormant", "still"), listOf(NATURE, MIND), "quiescence"),
    )

    val byId: Map<String, Word> = all.associateBy { it.id }

    fun search(query: String): List<Word> {
        val q = query.trim().lowercase()
        if (q.isBlank()) return all
        return all.filter {
            it.term.contains(q) ||
                it.definition.lowercase().contains(q) ||
                it.synonyms.any { synonym -> synonym.contains(q) }
        }
    }

    fun forThemes(themes: Set<String>): List<Word> {
        if (themes.isEmpty()) return all
        val matched = all.filter { word -> word.themes.any { it in themes } }
        return matched.ifEmpty { all }
    }
}
