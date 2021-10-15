/*
 * Copyright (c) 2021 Ubique Innovation AG <https://www.ubique.ch>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */

package ch.admin.bag.dp3t.vaccination

import androidx.recyclerview.widget.RecyclerView
import ch.admin.bag.dp3t.networking.models.VaccinationBookingCantonModel
import ch.admin.bag.dp3t.databinding.ItemVaccinationAppointmentCantonBinding

class VaccinationAppointmentCantonViewHolder(
	private val binding: ItemVaccinationAppointmentCantonBinding
) : RecyclerView.ViewHolder(binding.root) {

	fun bind(canton: VaccinationBookingCantonModel, onCantonClicked: (VaccinationBookingCantonModel) -> Unit) {
		binding.root.setOnClickListener { onCantonClicked.invoke(canton) }
		binding.cantonName.text = canton.name
	}

}