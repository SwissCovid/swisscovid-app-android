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

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ch.admin.bag.dp3t.networking.models.VaccinationBookingCantonModel
import ch.admin.bag.dp3t.databinding.ItemVaccinationAppointmentCantonBinding

class VaccinationAppointmentCantonAdapter(
	private val onCantonClicked: (VaccinationBookingCantonModel) -> Unit
) : RecyclerView.Adapter<VaccinationAppointmentCantonViewHolder>() {

	private val items = mutableListOf<VaccinationBookingCantonModel>()

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VaccinationAppointmentCantonViewHolder {
		val binding = ItemVaccinationAppointmentCantonBinding.inflate(LayoutInflater.from(parent.context), parent, false)
		return VaccinationAppointmentCantonViewHolder(binding)
	}

	override fun onBindViewHolder(holder: VaccinationAppointmentCantonViewHolder, position: Int) {
		holder.bind(items[position], onCantonClicked)
	}

	override fun getItemCount() = items.size

	fun setItems(newItems: List<VaccinationBookingCantonModel>) {
		items.clear()
		items.addAll(newItems)
		notifyDataSetChanged()
	}
}