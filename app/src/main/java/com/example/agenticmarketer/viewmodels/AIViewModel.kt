package com.example.agenticmarketer.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.agenticmarketer.repositories.AIRepository
import kotlinx.coroutines.launch

class AIViewModel : ViewModel() {
    private val repository = AIRepository()

    private val _uiState = MutableLiveData<AIUiState>()
    val uiState: LiveData<AIUiState> = _uiState

    fun generateContent(type: String, topic: String, extraInfo: String = "") {
    val prompt = when (type) {
        "blog" -> """
            You are writing a blog post like an experienced human writer who actually
            knows this topic — not like an SEO checklist being filled in. Follow the
            guidance below as a writer's mindset, not as a list of rules to tick off.

            Topic: $topic
            Tone of voice: $extraInfo

            VOICE & RHYTHM
            Write the way someone who actually understands this topic would explain it
            to a smart friend — casual but informed, not a lecture.
            - Let sentence length vary on its own. Some short. Some longer with a few
              clauses. Don't average them out to a "safe" medium length on purpose.
            - Don't open every paragraph with a transition word (Furthermore,
              Additionally, Moreover, As a result). Most human paragraphs don't start
              this way. Use transitions only where the logic actually needs one, and
              vary which one you reach for.
            - Avoid the "em dash setup — punchy payoff" sentence shape repeated more
              than once or twice in the whole post. It's a dead giveaway pattern.
              Same goes for "X: Y" colon constructions used as a rhetorical device.
            - Drop in the occasional short, blunt sentence. Real writers do this for
              emphasis. AI usually doesn't.
            - It's fine to have a small opinion or a mild aside ("honestly," "the
              annoying part is," "worth noting") — this is something AI text almost
              never does naturally.
            - Use contractions normally (it's, you'll, don't, that's) — don't write
              everything in full formal grammar.
            - Vary how examples are introduced. Not just "For example." Try "Say you,"
              "Take X," "Picture this," or just jumping straight into the example
              without a lead-in phrase at all.

            STRUCTURE (loose skeleton, not a rigid checklist)
            - A short intro that gets to the point quickly
            - A simple explanation of what the topic actually is
            - Why it matters right now
            - 2-3 main sections going deeper into the subject
            - A comparison table ONLY if you're actually comparing distinct options
            - A practical "how to" or step-by-step section if relevant
            - Common mistakes or pitfalls
            - A short FAQ section (3-5 real questions people would actually Google)
            - A closing thought — not a robotic summary of everything already said
            Don't force every single one of these into every post. Skip anything that
            doesn't genuinely fit this topic.

            FORMATTING
            - Paragraphs mostly short (2-4 sentences), but let one run longer
              occasionally if the thought needs it.
            - Subheadings should describe what's actually in that section.
            - Use bullet lists only when genuinely listing things, not to pad length.
            - Include a catchy title.

            LENGTH
            Aim for 1500-2000 words. Let the topic decide the exact number — don't
            pad weak sections just to hit a target, and don't cut a section short if
            it still has something useful to say.

            FINAL SELF-CHECK before finishing (think like an editor, not a
            rule-checker): Does any paragraph sound like it was written by following
            a formula? Did I start three sentences in a row the same way? Is there a
            sentence I'd actually say out loud to a friend, or does everything sound
            like a press release? Did I repeat the same transition word pattern more
            than twice? Fix what sounds robotic — don't just check boxes.
        """.trimIndent()

        "caption" -> """
            Act as a social media expert.
            Generate an engaging caption for $extraInfo about: $topic.
            Include relevant emojis and trending hashtags.
            Add a clear call to action (CTA).
        """.trimIndent()

        "humanize" -> """
            You are an experienced human editor reworking AI-generated text so it
            reads like it was written by a person who actually knows the subject —
            not like an AI doing a "make this sound human" pass. Apply this mindset
            throughout, not as a checklist.

            VOICE & RHYTHM
            - Let sentence length vary naturally. Some short. Some longer with a few
              clauses. Don't average everything out to a "safe" medium length.
            - Don't open every paragraph with a transition word (Furthermore,
              Additionally, Moreover, As a result). Use transitions only where the
              logic genuinely needs one, and vary which one you reach for.
            - Remove repeated "em dash setup — punchy payoff" sentence shapes and
              "X: Y" colon constructions used as a rhetorical device — these are
              dead giveaway AI patterns. One or two in the whole piece is fine, more
              than that needs rewriting.
            - Add the occasional short, blunt sentence for emphasis where it fits.
            - A small opinion or mild aside ("honestly," "the annoying part is,"
              "worth noting") is good — AI text almost never does this naturally.
            - Use contractions normally (it's, you'll, don't, that's).
            - Vary how examples are introduced — not just "For example" every time.

            WHAT TO PRESERVE
            Keep the original meaning, facts, and structure intact. This is a style
            and rhythm rewrite, not a content rewrite — don't add claims that weren't
            in the original, and don't drop information to make it shorter.

            FINAL SELF-CHECK before finishing: Does any paragraph sound like it
            follows a formula? Are three sentences in a row starting the same way?
            Is there at least one sentence that sounds like something you'd actually
            say out loud rather than write in a press release? Fix what sounds
            robotic — don't just check boxes.

            Text to rewrite:
            $topic
        """.trimIndent()

        "hashtags" ->
            "Generate 15 trending and relevant hashtags for the topic: $topic. Return only the hashtags separated by spaces."

        else -> ""
    }

    if (prompt.isEmpty()) return

    _uiState.value = AIUiState.Loading

    viewModelScope.launch {
        val (result, error) = repository.generateAIResponseWithError(prompt)

        if (result != null) {
            _uiState.value = AIUiState.Success(result)
        } else {
            _uiState.value = AIUiState.Error(
                error ?: "Failed to generate content."
            )
        }
    }
}

    private val _saveStatus = MutableLiveData<Boolean>()
    val saveStatus: LiveData<Boolean> = _saveStatus

    fun saveContent(topic: String, content: String, type: String) {
        viewModelScope.launch {
            val success = repository.saveToFirebase(topic, content, type)
            _saveStatus.value = success
        }
    }

    sealed class AIUiState {
        object Loading : AIUiState()
        data class Success(val content: String) : AIUiState()
        data class Error(val message: String) : AIUiState()
    }
}