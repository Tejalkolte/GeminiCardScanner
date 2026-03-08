package com.cardscanner.app

import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.cardscanner.app.databinding.FragmentResultBinding

class ResultFragment : Fragment() {
    private var _b: FragmentResultBinding? = null
    private val b get() = _b!!

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentResultBinding.inflate(i, c, false); return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        @Suppress("DEPRECATION")
        val contact: ContactInfo = arguments?.getParcelable("contact")!!

        b.editFirstName.setText(contact.firstName)
        b.editLastName.setText(contact.lastName)
        b.editOrganization.setText(contact.organization)
        b.editTitle.setText(contact.title)
        b.editPhone.setText(contact.phones.firstOrNull() ?: "")
        b.editEmail.setText(contact.emails.firstOrNull() ?: "")
        b.editWebsite.setText(contact.website)
        b.editAddress.setText(contact.address)

        b.btnSaveContact.setOnClickListener { saveContact() }
        b.btnScanAnother.setOnClickListener {
            findNavController().navigate(R.id.action_result_to_home)
        }
    }

    private fun saveContact() {
        startActivity(Intent(ContactsContract.Intents.Insert.ACTION).apply {
            type = ContactsContract.RawContacts.CONTENT_TYPE
            putExtra(ContactsContract.Intents.Insert.NAME,
                "${b.editFirstName.text} ${b.editLastName.text}".trim())
            putExtra(ContactsContract.Intents.Insert.COMPANY, b.editOrganization.text.toString())
            putExtra(ContactsContract.Intents.Insert.JOB_TITLE, b.editTitle.text.toString())
            val ph = b.editPhone.text.toString()
            if (ph.isNotBlank()) {
                putExtra(ContactsContract.Intents.Insert.PHONE, ph)
                putExtra(ContactsContract.Intents.Insert.PHONE_TYPE,
                    ContactsContract.CommonDataKinds.Phone.TYPE_WORK)
            }
            val em = b.editEmail.text.toString()
            if (em.isNotBlank()) {
                putExtra(ContactsContract.Intents.Insert.EMAIL, em)
                putExtra(ContactsContract.Intents.Insert.EMAIL_TYPE,
                    ContactsContract.CommonDataKinds.Email.TYPE_WORK)
            }
            val addr = b.editAddress.text.toString()
            if (addr.isNotBlank()) {
                putExtra(ContactsContract.Intents.Insert.POSTAL, addr)
            }
            val web = b.editWebsite.text.toString()
            if (web.isNotBlank())
                putExtra(ContactsContract.Intents.Insert.NOTES, "Website: $web")
        })
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
