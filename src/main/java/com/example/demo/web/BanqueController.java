package com.example.demo.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.dao.ClientRepository;
import com.example.demo.dao.CompteRepository;
import com.example.demo.entities.Client;
import com.example.demo.entities.Compte;
import com.example.demo.entities.Operation;
import com.example.demo.metier.IBanqueMetier;

@Controller
public class BanqueController {
	@Autowired
	private IBanqueMetier banqueMetier;
	
	@Autowired
	private CompteRepository compteRepository;
	@Autowired
	private ClientRepository clientRepository;
	
	@RequestMapping(value="/operations")
	public String index() {
		return "comptes";
	}
	@RequestMapping(value="/clients" , method=RequestMethod.GET)
	public String afficherClients(Model model) {
		List<Client> clients = clientRepository.findAll();
		model.addAttribute("lcit",clients);
		return "clients";
	}
	@RequestMapping(value="/frmAjoutCompte")
	public String frmAjoutCompte() {
		return "frmAjoutCompte";
	}
	@RequestMapping(value="/comptes", method=RequestMethod.GET)
	public String affichierComptes(Model model) {
		List<Compte> comptes = compteRepository.findAll();
		model.addAttribute("lcpt",comptes);
		return "frmListeComptes";
	}
	@PostMapping(value="/ajoutClient")
	public String ajoutClien(Model model ,@Validated @ModelAttribute("clients")Client c1 ,BindingResult BindingResult,
			RedirectAttributes flashMessages)throws Exception {
		//Client optionalClient = new Client();
		Client optionalClient = clientRepository.save(c1);
		return "redirect:/";
	}
	
	@RequestMapping(value="/consulterCompte", method = RequestMethod.GET)
	public String consulter(Model model,Long codeCompte,
			@RequestParam(name = "page",defaultValue = "0") int page ,
            @RequestParam(name = "size",defaultValue = "4") int size){

		try{
			model.addAttribute("codeCompte",codeCompte);
			
			Compte cp = banqueMetier.consulterCompte(codeCompte).get();
			
            Page<Operation> pageOperations = banqueMetier.listOperation(codeCompte,page,size);
            model.addAttribute("listOperations",pageOperations.getContent());
            int[] pages = new int[pageOperations.getTotalPages()];//nombre de pages
            model.addAttribute("pages",pages);
            model.addAttribute("compte",cp);
		}catch (Exception e){
			model.addAttribute("exception","Compte introuvable");
		}
		return "comptes";//meme vue comptes
	}
	
	 @RequestMapping(value="/saveOperation" ,method = RequestMethod.POST )
	    public String saveOperation(Model model ,  String typeOperation , Long codeCompte , double montant , Long codeCompte2){
	      try{
	          if(typeOperation.equals("VERS")){
	        	  banqueMetier.verser(codeCompte,montant);
	          }else if(typeOperation.equals("RETR")){
	        	  banqueMetier.retirer(codeCompte,montant);
	          }else if(typeOperation.equals("VIR")){
	        	  banqueMetier.virement(codeCompte,codeCompte2,montant);
	          }
	      }catch (Exception e){
	          model.addAttribute("error",e);
	          return "redirect:/consulterCompte?codeCompte="+codeCompte+"&error="+e.getMessage();
	      }

	        return "redirect:/consulterCompte?codeCompte="+codeCompte;
	    }
	
	 
	 @RequestMapping(value="/supprimerCompte/{codeCompte}")
		public String supprimerCompte(@PathVariable("codeCompte") long codeCompte) {
			  this.compteRepository.deleteById(codeCompte);
			  return "redirect:/";
		}
	
	 
}
