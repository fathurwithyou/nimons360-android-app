package com.eggheadengineers.nimons360.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.eggheadengineers.nimons360.domain.model.FamilyMember
import com.eggheadengineers.nimons360.ui.theme.PrimaryDark
import com.eggheadengineers.nimons360.ui.theme.PrimaryLight
import com.eggheadengineers.nimons360.ui.theme.TextSecondary

@Composable
fun AvatarCircle(initial: Char, size: Int = 32, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape),
        color = PrimaryLight,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = initial.toString(),
                style = MaterialTheme.typography.labelMedium,
                color = PrimaryDark,
            )
        }
    }
}

@Composable
fun MemberAvatarRow(
    members: List<FamilyMember>,
    total: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(AppGrid.Space2),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        members.forEach { member ->
            AvatarCircle(initial = member.name.firstOrNull()?.uppercaseChar() ?: '?', size = 28)
        }
        if (total > members.size) {
            Text(
                text = "+${total - members.size}",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
            )
        }
    }
}
