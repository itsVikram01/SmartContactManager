package com.scm.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.scm.dao.ContactRepository;
import com.scm.dao.UserRepository;
import com.scm.entities.Contact;
import com.scm.entities.User;
import com.scm.helper.Message;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ContactRepository contactRepository;

	// method for adding common data to response
	@ModelAttribute
	public void commonData(Model model, Principal principal) {
		System.out.println("Inside UserController ModelAttribute commonData ... ");
		String userName = principal.getName();
		System.out.println("USERNAME : " + userName);

		User user = userRepository.getUserByUserName(userName);
		System.out.println("USER : " + user);

		model.addAttribute("user", user);
	}

	// home dashboard
	@GetMapping("/index")
	public String dashboard(Model model, Principal principal) {
		System.out.println("Inside UserController dashboard handler... ");

		return "normal/user_dashboard";
	}

	// open add_contact_form
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model, Principal principal) {
		System.out.println("Inside UserController openAddContactForm handler... ");

		model.addAttribute("title", "Add Contact");
		model.addAttribute("contact", new Contact());

		return "normal/add_contact_form";
	}

	// processing contact form process-content
	@PostMapping("/process-contact")
	public String processContactForm(@ModelAttribute("contact") Contact contact,
			@RequestParam("profileImage") MultipartFile multipartFile, Principal principal, HttpSession session) {

		System.out.println("Inside UserController processContactForm handler... ");

		try {
			String name = principal.getName();
			User user = this.userRepository.getUserByUserName(name);

			System.out.println("DATA : " + contact);

			contact.setUser(user);
			user.getContacts().add(contact);

			// processing and uploading image/file
			if (multipartFile.isEmpty()) {
				System.out.println("File is empty!!...");
				contact.setImage("contact.png");
			} else {
				System.out.println("file getName : " + multipartFile.getName());
				System.out.println("file getOriginalFilename : " + multipartFile.getOriginalFilename());

				// update the file name to contact db
				contact.setImage(multipartFile.getOriginalFilename());
				System.out.println("File name added to database");

				// upload the file to folder
				File saveFile = new ClassPathResource("static/img").getFile();

				Path path = Paths
						.get(saveFile.getAbsolutePath() + File.separator + multipartFile.getOriginalFilename());

				Files.copy(multipartFile.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

				System.out.println("Image is uploaded!!...");
			}
			// save contact
			this.userRepository.save(user);

			// success message
			session.setAttribute("message", new Message("Contact is added Successfully!! Add More...", "success"));

		} catch (Exception e) {
			System.out.println("ERROR" + e.getMessage());
			e.printStackTrace();
			// error message
			session.setAttribute("message", new Message("Something went wrong? try again!!...", "danger"));
		}

		return "normal/add_contact_form";
	}

	// open view_contact_form
	// current page no = [page] : 0
	// contact per page = [n] : 5
	@GetMapping("/view-contacts/{page}")
	public String openViewContactsForm(@PathVariable("page") Integer page, Model model, Principal principal) {
		System.out.println("Inside UserController openViewContactsForm handler... ");
		model.addAttribute("title", "View Contact");

		// get user list
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		/*
		 * // get contact list for all users List<Contact> contacts =
		 * user.getContacts();
		 */

		// current page no = [page] : 0
		// contact per page = [n] : 5
		Pageable pageable = PageRequest.of(page, 2);

		// get contact list from ContactRepository for login user
		Page<Contact> contacts = this.contactRepository.findContactsByUser(user.getId(), pageable);
		model.addAttribute("contacts", contacts);
		model.addAttribute("currentPage", page);
		model.addAttribute("tatalPages", contacts.getTotalPages());

		return "normal/view_contacts_form";
	}

	// showing particular user contact detail
	@GetMapping("/{cId}/contact")
	public String showContactDetail(@PathVariable("cId") Integer cId, Model model, Principal principal) {
		System.out.println("Inside UserController showContactDetail handler... ");
		System.out.println("CID : " + cId);

		Optional<Contact> contactOptional = this.contactRepository.findById(cId);
		Contact contact = contactOptional.get();

		// user logged in
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);

		if (user.getId() == contact.getUser().getId()) {
			model.addAttribute("contact", contact);
			model.addAttribute("title", contact.getName());
		}

		return "normal/contact_detail";
	}

	// update contact
	@PostMapping("/update-contact/{cid}")
	public String updateContact(@PathVariable("cid") Integer cId, Model model, HttpSession session) {
		System.out.println("Inside UserController updateContact handler... ");

		model.addAttribute("title", "Update Contact");

		Contact contact = this.contactRepository.findById(cId).get();

		model.addAttribute("contact", contact);

		// session.setAttribute("message", new Message("Contact updated
		// succesfully...!!", "success"));

		return "normal/update_contact";
	}

	// process update contact
	// processing update-contact form process-update-contact
	@PostMapping("/process-update-contact")
	public String processUpdateContactForm(@ModelAttribute("contact") Contact contact,
			@RequestParam("profileImage") MultipartFile multipartFile, Principal principal, HttpSession session) {
		System.out.println("Inside UserController processUpdateContactForm handler... ");
		System.out.println("CONTACT : " + contact);

		try {
			// old contact detail to delete image
			Contact oldContactDetails = this.contactRepository.findById(contact.getcId()).get();

			// System.out.println("multipartFile getName : "+multipartFile.getName());
			// System.out.println("multipartFile getOriginalFilename :
			// "+multipartFile.getOriginalFilename());

			// processing and uploading new image/file
			if (!multipartFile.isEmpty()) {
				System.out.println("File is not empty!!...");

				// delete old image
				File deleteFile = new ClassPathResource("static/img").getFile();
				File file = new File(deleteFile, oldContactDetails.getImage());
				file.delete();

				// update the new file to folder
				File saveFile = new ClassPathResource("static/img").getFile();
				Path path = Paths
						.get(saveFile.getAbsolutePath() + File.separator + multipartFile.getOriginalFilename());
				Files.copy(multipartFile.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

				// update the new file name to contact db
				contact.setImage(multipartFile.getOriginalFilename());

				System.out.println("File name added to database");

				System.out.println("Image is uploaded!!...");
			} else {
				System.out.println("File is empty!!...");
				// contact.setImage("contact.png");
				contact.setImage(oldContactDetails.getImage());
			}
			// String name = principal.getName();
			// User user = this.userRepository.getUserByUserName(name);
			User user = this.userRepository.getUserByUserName(principal.getName());

			contact.setUser(user);

			// save contact
			this.contactRepository.save(contact);

			// success message
			session.setAttribute("message", new Message("Contact is updated Successfully...!!", "success"));
		} catch (Exception e) {
			System.out.println("ERROR" + e.getMessage());
			e.printStackTrace();
			// error message
			session.setAttribute("message", new Message("Something went wrong? try again...!!", "danger"));
		}
		System.out.println("CONTACT NAME : " + contact.getName());
		System.out.println("CONTACT ID : " + contact.getcId());
		return "redirect:/user/" + contact.getcId() + "/contact";
	}

	// delete user contact
	@GetMapping("/delete/{cid}")
	public String deleteContact(@PathVariable("cid") Integer cId, Model model, HttpSession session) {
		System.out.println("Inside UserController deleteContact handler... ");

		// Optional<Contact> contactOptional = this.contactRepository.findById(cId);
		// Contact contact = contactOptional.get();
		Contact contact = this.contactRepository.findById(cId).get();

		// set contact = null to unlink from contact so that it could delete
		contact.setUser(null);
		// delete contact
		this.contactRepository.delete(contact);

		session.setAttribute("message", new Message("Contact deleted succesfully...!!", "success"));

		return "redirect:/user/view-contacts/0";
	}

	// open user-settings
	@GetMapping("/user-settings")
	public String openUserSettingForm(Model model, Principal principal) {
		System.out.println("Inside UserController openUserSettingForm handler... ");

		model.addAttribute("title", "Settings");
		model.addAttribute("contact", new Contact());

		return "normal/settings";
	}
}
