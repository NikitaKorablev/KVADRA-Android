package ru.kvadra_app.contacts_list.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.kvadra_app.contacts_list.R
import ru.kvadra_app.contacts_list.presentation.theme.Blue
import ru.kvadra_app.contacts_list.presentation.theme.ContactsTheme
import ru.kvadra_app.model.Contact
import ru.kvadra_app.model.ContactItem

@Composable
fun ContactItemRow(
    contact: Contact,
    onContactClick: (Contact) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .border(1.dp, Blue, RoundedCornerShape(0.dp))
                .clickable { onContactClick(contact) }
                .padding(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(Blue, CircleShape)
            )
            Spacer(modifier = Modifier.width(15.dp))
            Column(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = contact.name,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = contact.phoneNumber,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun HeaderItemRow(
    header: Char,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 18.dp, bottom = 5.dp)
            .border(1.dp, Blue, RoundedCornerShape(0.dp))
            .background(Color.Transparent)
    ) {
        Text(
            text = header.toString(),
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 10.dp),
            color = Color.Black,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
    }
}

@Composable
fun ContactListScreen(
    contactItems: List<ContactItem>,
    onContactClick: (Contact) -> Unit,
    onServiceButtonClick: () -> Unit,
    isEmpty: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        if (isEmpty) {
            Text(
                text = stringResource(R.string.empty_contacts_message),
                modifier = Modifier.align(Alignment.Center),
                fontSize = 18.sp,
                color = Color.Black
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 70.dp) // Leave space for button
            ) {
                items(contactItems) { item ->
                    if (item.header != null) {
                        HeaderItemRow(header = item.header!!)
                    } else if (item.contact != null) {
                        ContactItemRow(
                            contact = item.contact!!,
                            onContactClick = onContactClick
                        )
                    }
                }
            }
        }

        Button(
            onClick = onServiceButtonClick,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Blue),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = stringResource(R.string.button_text),
                color = Color.White
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HeaderItemPreview() {
    ContactsTheme {
        HeaderItemRow(header = 'A')
    }
}

@Preview(showBackground = true)
@Composable
fun ContactListScreenPreview() {
    ContactsTheme {
        ContactListScreen(
            contactItems = listOf(
                ContactItem(contact = null, header = 'A'),
                ContactItem(contact = Contact("Alice", "111", 0, 0), header = null),
                ContactItem(contact = Contact("Andrew", "222", 0, 0), header = null),
                ContactItem(contact = null, header = 'B'),
                ContactItem(contact = Contact("Bob", "333", 0, 0), header = null)
            ),
            onContactClick = {},
            onServiceButtonClick = {},
            isEmpty = false
        )
    }
}
