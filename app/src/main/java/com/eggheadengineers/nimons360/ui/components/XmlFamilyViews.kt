package com.eggheadengineers.nimons360.ui.components

import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import com.eggheadengineers.nimons360.R
import com.eggheadengineers.nimons360.databinding.ItemFamilyRowBinding
import com.eggheadengineers.nimons360.domain.model.Family
import com.eggheadengineers.nimons360.domain.model.FamilyMember

private fun Family.displayMemberCount(): Int? = memberCount ?: members.takeIf { it.isNotEmpty() }?.size

private fun resolveUserImageUrl(value: String): String =
    if (value.startsWith("http://") || value.startsWith("https://")) {
        value
    } else {
        "https://mad.labpro.hmif.dev/${value.removePrefix("/")}"
    }

private fun bindMemberAvatar(member: FamilyMember?, initialView: TextView, imageView: ImageView) {
    val imageUrl = member?.profileImageUrl?.takeIf { it.isNotBlank() }?.let(::resolveUserImageUrl)
    initialView.isVisible = member != null && imageUrl == null
    imageView.isVisible = member != null && imageUrl != null
    initialView.text = member?.name?.firstOrNull()?.uppercaseChar()?.toString().orEmpty()
    if (imageUrl != null) {
        imageView.loadMemberProfileUrl(imageUrl)
    } else {
        imageView.setImageDrawable(null)
    }
}

fun renderFamilyRows(
    inflater: LayoutInflater,
    container: LinearLayout,
    families: List<Family>,
    pinnedIds: Set<String> = emptySet(),
    pinMode: Boolean = false,
    onFamilyClick: (Family) -> Unit,
    onPinClick: ((Family) -> Unit)? = null,
) {
    container.removeAllViews()
    families.forEachIndexed { index, family ->
        val binding = ItemFamilyRowBinding.inflate(inflater, container, false)
        binding.familyName.text = family.name
        binding.familyMeta.text = family.displayMemberCount()?.let { "$it members" } ?: "Members unavailable"
        binding.familyIcon.loadUrl(family.iconUrl)
        binding.familyRowContent.applyBoundedRipple(cornerRadiusDp = 12f)
        binding.familyRowContent.applyElasticPress()
        binding.familyRowContent.setOnClickListener { onFamilyClick(family) }

        val visibleMembers = family.members.take(3)
        val avatarViews = listOf(
            binding.familyAvatarOne to binding.familyAvatarOneImage,
            binding.familyAvatarTwo to binding.familyAvatarTwoImage,
            binding.familyAvatarThree to binding.familyAvatarThreeImage,
        )
        avatarViews.forEachIndexed { avatarIndex, (textView, imageView) ->
            val member = visibleMembers.getOrNull(avatarIndex)
            bindMemberAvatar(member, textView, imageView)
        }
        binding.familyAvatarGroup.isVisible = visibleMembers.isNotEmpty()
        val overflow = family.members.size - visibleMembers.size
        binding.familyAvatarOverflow.isVisible = overflow > 0
        binding.familyAvatarOverflow.text = if (overflow > 0) "+$overflow" else ""

        binding.familyPinButton.isVisible = pinMode
        if (pinMode) {
            val pinned = family.id in pinnedIds
            binding.familyPinButton.setImageResource(
                if (pinned) R.drawable.ic_xml_pin_filled else R.drawable.ic_xml_pin_outline
            )
            binding.familyPinButton.applyBorderlessRipple()
            binding.familyPinButton.setOnClickListener { onPinClick?.invoke(family) }
        }

        binding.familyDivider.visibility = if (index == families.lastIndex) View.GONE else View.VISIBLE
        container.addView(binding.root)
    }
}
