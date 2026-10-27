package com.samuel.farmos.ui.components

import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.samuel.farmos.data.Animal
import com.samuel.farmos.ui.screens.FarmUi

enum class TriageUrgency(
    val title: String,
    val lightColor: Color,
    val darkColor: Color
) {
    CRITICAL("🚨 CRITICAL (Emergency)", Color(0xFFC62828), Color(0xFFEF5350)),
    URGENT("🟠 URGENT (<12 hrs)", Color(0xFFE65100), Color(0xFFFF9800)),
    MANAGEABLE("🟡 FARM FIRST-AID", Color(0xFFD97706), Color(0xFFFBBF24))
}

enum class BodyZone(val title: String, val emoji: String) {
    UDDER("Udder & Teats", "🥛"),
    HEAD("Head, Eyes & Mouth", "🐮"),
    HOOVES("Hooves & Mobility", "🦶"),
    RUMEN("Rumen & Digestion", "🌾"),
    CALVING("Calving & Downer", "🍼")
}

data class DiagnosticScenario(
    val id: String,
    val zone: BodyZone,
    val observedSymptoms: String,
    val conditionName: String,
    val urgency: TriageUrgency,
    val clinicalBackground: String,
    val immediateActions: List<String>,
    val strictDoNots: String
)

@Composable
fun CowSymptomDiagnosticView(
    animals: List<Animal> = emptyList(),
    farmName: String = "My Farm",
    onReportCaseWithDetails: ((complaint: String, animalId: String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedZone by remember { mutableStateOf(BodyZone.UDDER) }
    var expandedScenarioId by remember { mutableStateOf<String?>(null) }
    var selectedAnimalTag by remember { mutableStateOf("") }

    val scenarios = remember { getDiagnosticScenarios() }
    val filteredScenarios = scenarios.filter { it.zone == selectedZone }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // Top Triage Banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isSystemInDarkTheme()) Color(0xFF1E2F27) else Color(0xFFEAF5EE)
            ),
            border = BorderStroke(1.dp, FarmUi.green.copy(alpha = 0.4f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(FarmUi.green.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MedicalServices,
                        contentDescription = null,
                        tint = FarmUi.green,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "🩺 Vet Triage & First-Aid Desk",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = FarmUi.green
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Interactive symptom checker, urgency triage & immediate emergency protocols.",
                        style = MaterialTheme.typography.bodySmall,
                        color = FarmUi.ink,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Animal Tag Selector (if animals are present)
        if (animals.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Select Patient Cow:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = FarmUi.textMuted
                )
                if (selectedAnimalTag.isNotBlank()) {
                    Text(
                        text = "Clear",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = FarmUi.coral,
                        modifier = Modifier.clickable { selectedAnimalTag = "" }
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(animals) { animal ->
                    val isSelected = selectedAnimalTag == animal.tagNumber
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) FarmUi.teal else FarmUi.surface,
                        border = BorderStroke(1.dp, if (isSelected) FarmUi.teal else FarmUi.cardBorder),
                        modifier = Modifier.clickable {
                            selectedAnimalTag = if (isSelected) "" else animal.tagNumber
                        }
                    ) {
                        Text(
                            text = "${animal.tagNumber} ${if (animal.name.isNotBlank()) "(${animal.name})" else ""}",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else FarmUi.ink
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Body Zone Filter Row
        Text(
            text = "Select Observed Body Zone:",
            modifier = Modifier.padding(horizontal = 16.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = FarmUi.textMuted
        )
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(BodyZone.values()) { zone ->
                val isSelected = selectedZone == zone
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) FarmUi.green else FarmUi.surface,
                    border = BorderStroke(1.dp, if (isSelected) FarmUi.green else FarmUi.cardBorder),
                    modifier = Modifier.clickable {
                        selectedZone = zone
                        expandedScenarioId = null
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(zone.emoji, fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = zone.title,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                            color = if (isSelected) Color.White else FarmUi.ink
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Diagnostic Cards for Selected Zone
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            filteredScenarios.forEach { scenario ->
                DiagnosticCard(
                    scenario = scenario,
                    isExpanded = expandedScenarioId == scenario.id,
                    selectedAnimalTag = selectedAnimalTag,
                    farmName = farmName,
                    onToggleExpand = {
                        expandedScenarioId = if (expandedScenarioId == scenario.id) null else scenario.id
                    },
                    onShareSos = {
                        shareVetSos(context, farmName, selectedAnimalTag, scenario)
                    },
                    onLogCase = {
                        onReportCaseWithDetails?.invoke(
                            "${scenario.conditionName}: ${scenario.observedSymptoms}",
                            selectedAnimalTag
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun DiagnosticCard(
    scenario: DiagnosticScenario,
    isExpanded: Boolean,
    selectedAnimalTag: String,
    farmName: String,
    onToggleExpand: () -> Unit,
    onShareSos: () -> Unit,
    onLogCase: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val urgencyColor = if (isDark) scenario.urgency.darkColor else scenario.urgency.lightColor
    val urgencyBg = urgencyColor.copy(alpha = 0.12f)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = FarmUi.surface),
        border = BorderStroke(
            1.dp,
            if (isExpanded) urgencyColor.copy(alpha = 0.5f) else FarmUi.cardBorder
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row: Observed Symptom + Urgency Pill
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = urgencyBg,
                        border = BorderStroke(1.dp, urgencyColor.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = scenario.urgency.title,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = urgencyColor
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = scenario.observedSymptoms,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = FarmUi.ink
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Suspected: ${scenario.conditionName}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = FarmUi.green
                    )
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = FarmUi.textMuted,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Expanded First-Aid Protocol
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(tween(250)) + fadeIn(tween(250)),
                exit = shrinkVertically(tween(200)) + fadeOut(tween(200))
            ) {
                Column(modifier = Modifier.padding(top = 14.dp)) {
                    HorizontalDivider(color = FarmUi.cardBorder)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Clinical Background
                    Text(
                        text = "Clinical Overview:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = FarmUi.textMuted
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = scenario.clinicalBackground,
                        style = MaterialTheme.typography.bodySmall,
                        color = FarmUi.ink,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Immediate First-Aid Steps
                    Text(
                        text = "🚑 Immediate First-Aid Protocol:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = FarmUi.green
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    scenario.immediateActions.forEachIndexed { index, action ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = FarmUi.teal.copy(alpha = 0.15f),
                                modifier = Modifier.size(20.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "${index + 1}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = FarmUi.teal
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = action,
                                style = MaterialTheme.typography.bodySmall,
                                color = FarmUi.ink,
                                modifier = Modifier.weight(1f),
                                lineHeight = 18.sp
                            )
                        }
                    }

                    // Strict DO NOT Warning
                    if (scenario.strictDoNots.isNotBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = FarmUi.coral.copy(alpha = 0.1f),
                            border = BorderStroke(1.dp, FarmUi.coral.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = FarmUi.coral,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "DO NOT: ${scenario.strictDoNots}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = FarmUi.coral,
                                    lineHeight = 17.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action Buttons: Share SOS to WhatsApp & Log as Health Case
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onShareSos,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isDark) Color(0xFF25D366) else Color(0xFF1EBE5D),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Send Vet SOS", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                        }

                        OutlinedButton(
                            onClick = onLogCase,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, FarmUi.teal)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = FarmUi.teal, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Log Illness", color = FarmUi.teal, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
        }
    }
}

private fun shareVetSos(
    context: Context,
    farmName: String,
    animalTag: String,
    scenario: DiagnosticScenario
) {
    val sb = StringBuilder()
    sb.appendLine("🚨 *FarmOS Veterinary SOS Alert*")
    sb.appendLine("🏡 Farm: ${farmName.ifBlank { "My Farm" }}")
    if (animalTag.isNotBlank()) {
        sb.appendLine("🏷️ Animal Tag: $animalTag")
    }
    sb.appendLine("📍 Suspected: *${scenario.conditionName}*")
    sb.appendLine("⚠️ Urgency: *${scenario.urgency.title}*")
    sb.appendLine("🔍 Symptoms Observed: ${scenario.observedSymptoms}")
    sb.appendLine("\n🚑 *First-Aid Initiated On Farm:*")
    scenario.immediateActions.take(2).forEach { action ->
        sb.appendLine("• $action")
    }
    if (scenario.strictDoNots.isNotBlank()) {
        sb.appendLine("⚠️ Critical Note: ${scenario.strictDoNots}")
    }
    sb.appendLine("\n📞 *Doctor, please advise or visit as soon as possible.*")
    sb.appendLine("_Sent via FarmOS Vet Desk_")

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, sb.toString())
    }
    val chooser = Intent.createChooser(intent, "Send Vet SOS via WhatsApp / Message").apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(chooser)
}

private fun getDiagnosticScenarios(): List<DiagnosticScenario> = listOf(
    // 1. Udder & Teats
    DiagnosticScenario(
        id = "ud_1",
        zone = BodyZone.UDDER,
        observedSymptoms = "Swollen, hard, hot quarter + flakes or clots in milk",
        conditionName = "Acute Clinical Mastitis",
        urgency = TriageUrgency.URGENT,
        clinicalBackground = "Bacterial infection (Staph or Strep) inflaming mammary tissue, causing milk clotting and pain.",
        immediateActions = listOf(
            "Strip out the infected quarter thoroughly every 2–3 hours into a strip cup (never onto the floor).",
            "Apply cold water or ice pack for 10–15 minutes to reduce inflammation.",
            "Infuse intramammary antibiotic tube as prescribed by your vet and massage gently upwards.",
            "WITHHOLD MILK: Do not mix milk from this cow with the bulk milk tank."
        ),
        strictDoNots = "Do NOT leave infected milk in the udder or strip onto bedding—it rapidly spreads to other cows."
    ),
    DiagnosticScenario(
        id = "ud_2",
        zone = BodyZone.UDDER,
        observedSymptoms = "Painful teat cracks, scabs, or wart-like growths",
        conditionName = "Teat Papillomatosis / Chapped Teats",
        urgency = TriageUrgency.MANAGEABLE,
        clinicalBackground = "Viral or cold-wind damage compromising teat skin elasticity, making milking painful and risking mastitis.",
        immediateActions = listOf(
            "Apply veterinary teat salve or petroleum jelly with zinc oxide after milking.",
            "Ensure post-milking teat dip (iodine-based) is active and fresh.",
            "Milk this animal last in the milking order to prevent spreading to herd mates."
        ),
        strictDoNots = "Do NOT squeeze or pull scabbed teats aggressively without emollient lubricant."
    ),
    DiagnosticScenario(
        id = "ud_3",
        zone = BodyZone.UDDER,
        observedSymptoms = "Pink or reddish milk with no heat or swelling in udder",
        conditionName = "Ruptured Capillaries (Post-Calving Udder Edema)",
        urgency = TriageUrgency.MANAGEABLE,
        clinicalBackground = "Heavy milk engorgement right after calving straining tiny blood vessels inside the udder.",
        immediateActions = listOf(
            "Milk gently twice daily without harsh pulling.",
            "Avoid deep vigorous udder massage which can worsen capillary bleeding.",
            "Usually resolves naturally in 3 to 5 days as swelling subsides."
        ),
        strictDoNots = "Do NOT administer antibiotics unless fever or hard quarters develop."
    ),

    // 2. Head, Eyes & Saliva
    DiagnosticScenario(
        id = "hd_1",
        zone = BodyZone.HEAD,
        observedSymptoms = "Excessive ropey drooling, mouth sores, blistered tongue & hooves",
        conditionName = "Suspected Foot & Mouth Disease (FMD)",
        urgency = TriageUrgency.CRITICAL,
        clinicalBackground = "Highly contagious Aphthovirus causing severe mouth ulcerations, lameness, and production collapse.",
        immediateActions = listOf(
            "Immediately isolate the cow in a quarantine enclosure at least 50m away from herd.",
            "Stop all animal, person, and vehicle movements on and off the farm.",
            "Wash mouth with mild 1% alum solution or diluted sodium bicarbonate to soothe pain.",
            "Notify local veterinary officer or livestock authorities immediately."
        ),
        strictDoNots = "Do NOT sell or transport milk, dung, or animals from the farm—FMD is a quarantined state emergency."
    ),
    DiagnosticScenario(
        id = "hd_2",
        zone = BodyZone.HEAD,
        observedSymptoms = "Cloudy white/pink eye ulcer, tears streaming, squinting in sun",
        conditionName = "Infectious Bovine Keratoconjunctivitis (Pinkeye)",
        urgency = TriageUrgency.URGENT,
        clinicalBackground = "Moraxella bovis bacterial infection transmitted by face flies, exacerbated by bright sunlight and dust.",
        immediateActions = listOf(
            "Move cow to a dark, well-shaded shed (sunlight causes intense corneal pain).",
            "Gently flush tears with sterile saline solution.",
            "Apply topical antibiotic eye ointment (e.g. Oxytetracycline) inside the lower eyelid.",
            "Apply pour-on fly repellent to face to stop fly transmission."
        ),
        strictDoNots = "Do NOT blow ground salt, glass powder, or toxic ash into the eyes (destroys cornea)."
    ),
    DiagnosticScenario(
        id = "hd_3",
        zone = BodyZone.HEAD,
        observedSymptoms = "Swollen parotid lymph node below ear, high fever (40°C+), labored breathing",
        conditionName = "East Coast Fever (ECF - Theileriosis)",
        urgency = TriageUrgency.CRITICAL,
        clinicalBackground = "Protozoan parasite transmitted by Rhipicephalus appendiculatus (brown ear tick), fatal if untreated past 48h.",
        immediateActions = listOf(
            "Check ears and under tail for brown ticks and remove them.",
            "Check rectal temperature immediately with a digital thermometer.",
            "Summon vet urgently for specific Buparvaquone (Butalex) injection + supportive diuretic."
        ),
        strictDoNots = "Do NOT delay calling the vet—ECF mortality exceeds 90% in exotic/crossbred dairy cows if untreated."
    ),

    // 3. Hooves & Mobility
    DiagnosticScenario(
        id = "hf_1",
        zone = BodyZone.HOOVES,
        observedSymptoms = "Sudden severe limping, swollen coronet, foul rotting odor between claws",
        conditionName = "Foot Rot (Interdigital Necrobacillosis)",
        urgency = TriageUrgency.URGENT,
        clinicalBackground = "Bacterial invasion through moist, softened skin between hoof claws, causing deep tissue necrosis.",
        immediateActions = listOf(
            "Clean the hoof thoroughly with clean water to inspect the gap between claws.",
            "Stand foot in a 5% copper sulfate or zinc sulfate footbath for 5–10 minutes.",
            "House the cow on clean, dry straw bedding away from wet mud.",
            "Vet will administer systemic long-acting oxytetracycline or penicillin."
        ),
        strictDoNots = "Do NOT leave cow standing in deep wet manure or mud slurry."
    ),
    DiagnosticScenario(
        id = "hf_2",
        zone = BodyZone.HOOVES,
        observedSymptoms = "Stiff reluctance to walk, arched back, front hooves outstretched after grain binge",
        conditionName = "Acute Laminitis (Founder from Grain Acidosis)",
        urgency = TriageUrgency.CRITICAL,
        clinicalBackground = "Sudden high starch intake fermenting in rumen, releasing lactic acid and histamine that inflames hoof corium.",
        immediateActions = listOf(
            "Immediately withdraw all concentrate feed, grain, and dairy meal.",
            "Provide dry hay or straw and fresh clean water ad-lib.",
            "Drench with 200g sodium bicarbonate in 1L warm water to neutralize rumen acidity.",
            "Cold water hosing of hooves for 15 minutes to reduce claw throbbing."
        ),
        strictDoNots = "Do NOT force the cow to walk long distances on hard gravel or concrete."
    ),

    // 4. Rumen & Digestion
    DiagnosticScenario(
        id = "rm_1",
        zone = BodyZone.RUMEN,
        observedSymptoms = "Left flank ballooned taut like a drum, grunting, tongue hanging out",
        conditionName = "Acute Frothy / Free-Gas Bloat",
        urgency = TriageUrgency.CRITICAL,
        clinicalBackground = "Trapped gas or foam in the rumen pressing against the diaphragm, suffocating the lungs within hours.",
        immediateActions = listOf(
            "Stand the cow with front feet elevated on an incline or mound to help gas escape.",
            "Keep mouth open with a wooden bit or piece of rope tied behind horns.",
            "Drench with 250–350ml cooking oil, mineral oil, or bloat remedy (breaks the foam bubbles).",
            "Emergency: If cow is collapsing from suffocation, an emergency trocar/cannula in the left paralumbar fossa is required."
        ),
        strictDoNots = "Do NOT allow the cow to lie flat on her side; keep her upright and moving gently."
    ),
    DiagnosticScenario(
        id = "rm_2",
        zone = BodyZone.RUMEN,
        observedSymptoms = "Watery dark or bloody foul diarrhea, sunken eyes, rapid weight loss",
        conditionName = "Bovine Coccidiosis / Enteritis",
        urgency = TriageUrgency.URGENT,
        clinicalBackground = "Parasitic or bacterial destruction of intestinal lining causing severe dehydration and mineral depletion.",
        immediateActions = listOf(
            "Immediately isolate from other cattle and clean the pen.",
            "Provide 5–10L of warm oral electrolyte rehydration solution per day.",
            "Ensure access to clean dry hay (no lush fresh grass until diarrhea stops).",
            "Vet must prescribe specific anticoccidial (Amprolium / Sulphonamide)."
        ),
        strictDoNots = "Do NOT withhold water—dehydration kills faster than the infection itself."
    ),

    // 5. Calving & Downer
    DiagnosticScenario(
        id = "cl_1",
        zone = BodyZone.CALVING,
        observedSymptoms = "Unable to stand 12–48h after calving, cold ears & dry nose, S-curved neck",
        conditionName = "Milk Fever (Parturient Hypocalcemia)",
        urgency = TriageUrgency.CRITICAL,
        clinicalBackground = "Sudden drainage of blood calcium into colostrum, causing muscle weakness, paralysis, and cardiac failure.",
        immediateActions = listOf(
            "Prop the cow upright in a sternal position (sitting on chest) supported by hay bales—NEVER let her lie flat on side.",
            "Cover with warm blankets or dry sacks to combat hypothermia.",
            "Call vet urgently for Calcium Borogluconate 40% slow intravenous & subcutaneous infusion.",
            "Once calcium is given, allow time for muscle tone to return before encouraging her to stand."
        ),
        strictDoNots = "CRITICAL: Do NOT force liquids or medicine down her throat! Swallowing muscles are paralyzed; liquids go straight to lungs, causing fatal drowning/aspiration pneumonia."
    ),
    DiagnosticScenario(
        id = "cl_2",
        zone = BodyZone.CALVING,
        observedSymptoms = "Afterbirth membrane still hanging from vulva >12 hours post-calving",
        conditionName = "Retained Placenta (Fetal Membranes)",
        urgency = TriageUrgency.URGENT,
        clinicalBackground = "Failure of the placenta to detach from uterine caruncles within 12 hours of delivery, risking metritis and toxemia.",
        immediateActions = listOf(
            "Trim the hanging portion cleanly 10cm below the hocks with clean scissors so she doesn't step on it.",
            "Keep the tail and perineum washed with mild antiseptic water.",
            "Monitor body temperature daily for fever (>39.2°C).",
            "Vet will administer systemic antibiotic and intrauterine pessaries."
        ),
        strictDoNots = "CRITICAL: NEVER forcibly yank or pull on the retained membrane! This tears uterine caruncles, causing fatal uterine hemorrhage and permanent infertility."
    )
)
