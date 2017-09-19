import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;

public class InterpreterController extends Application{

	private int CommentCount = 0;
	private int QuestionCount = 0;
	private String yourText = "";
	private HashSet<String> CommentHash = new HashSet<String>();
	private HashSet<String> QuestionHash = new HashSet<String>();
	private NoteNode rootNode = new NoteNode(null,new ArrayList<NoteNode>(),null);
	private String saveName = "save";
	private String[] prevlets;
	
    @FXML
    private TextArea QuestionsArea;
    
    @FXML
    private Button load;

    @FXML
    private AnchorPane Comments;

    @FXML
    private Button save;
    @FXML
    private AnchorPane Questions;

    @FXML
    private TextArea CommentsArea;
    
    @FXML
    private TextArea TextOut;

    @FXML
    private TextArea CodeIn;
    
    private void processString(String s){
		yourText = s;
		String news = "";
		for(char c: s.toCharArray()){
			if(c == '\n'){
				news+="\n ";
			}
			else{
				news+=c;
			}
		}
		s = news;
		String[] split = s.split("\n");
		String str = split[split.length -2];
		if(str.charAt(0) == ' '){
			str = str.substring(1);
		}
		String firstword = str.split(" ")[0];
		if(str.length() != 0 && firstword.charAt(0) == '#'){
			if(CommentHash.add(str.toLowerCase())){
				CommentCount++;
				CommentsArea.appendText(CommentCount +") "+ str.substring(1)+"\n");
			}
		}
		else if(str.length() != 0 && firstword.charAt(0) == '?'){
			if(QuestionHash.add(str.toLowerCase())){
				QuestionCount++;
				QuestionsArea.appendText(QuestionCount +") "+ str.substring(1)+"\n");
			}
		}
		else if(str.length() != 0 && firstword.charAt(0) == '@'){
			saveName = firstword.substring(1);
		}
		else if(firstword.length() >= 1 && firstword.charAt(firstword.length()-1) == ')'){
			String[] location = ParseLetters(firstword);
			for(int i = 0; i < location.length; i++){
				if(Character.isLetter(location[i].charAt(0))){
					location[i] = Integer.toString(NoteNode.convertString(location[i]));
				}
			}
			NoteNode curnode = rootNode;
			for(int i = 0; i < location.length; i++){
				String stradd = "";
				NoteNode nextNode;
				if(i == location.length -1){
					stradd = str.substring(str.indexOf(')')+2);
				}
				if(Integer.parseInt(location[i])> curnode.children.size()){
					for(int y = curnode.children.size(); y<Integer.parseInt(location[i])-1; y++){
						curnode.children.add(null);
					}
					nextNode = new NoteNode(stradd,new ArrayList<NoteNode>(),curnode);
					curnode.children.add(nextNode);
					
				}
				else{
					if(!stradd.equals("")){
						stradd = "\n"+stradd;
					}
					nextNode = curnode.children.get(Integer.parseInt(location[i])-1);
					nextNode.value += stradd;
				}
				int c = 0;
				curnode = nextNode;
			}
			TextOut.setText("");
			displayNodes(rootNode,0);
			CodeIn.appendText(getNextString(prevlets)+") ");
		}
	}
    
    private String getNextString(String[] lets){
    	String result = "";
    	String r;
    	for(int i = 0; i < lets.length; i++){
    		if(i == lets.length-1){
    			if(i%2 == 0){
    				int s = Integer.parseInt(lets[i]);
    				s++;
    				r = NoteNode.getString(s);
    				result+= r;
    			}
    			else{
    				result += String.valueOf(Integer.parseInt(lets[i])+1);
    			}
    		}
    		else{
    			if(i%2 == 0){
    				int s = Integer.parseInt(lets[i]);
    				r = NoteNode.getString(s);
    				result+= r;
    			}
    			else{
    				result += String.valueOf(Integer.parseInt(lets[i]));
    			}
    		}
    	}
    	return result;
    }
    
    @FXML
	private void initialize() {
    	CodeIn.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable,
					String oldValue, String newValue) {
				String s = CodeIn.getText();
				if(s.length() > 3 && newValue.equals(oldValue + "\n")){
					processString(s);
				}
				if(s.length() > 3 && newValue.equals(oldValue + "\t")){
					if(s.substring(s.length()-3).equals(") \t")){
				    	String result = "";
				    	String r;
						for(int i = 0; i < prevlets.length; i++){
			    			if(i%2 == 0){
			    				int see = Integer.parseInt(prevlets[i]);
			    				r = NoteNode.getString(see);
			    				result+= r;
			    			}
			    			else{
			    				result += String.valueOf(Integer.parseInt(prevlets[i]));
			    			}
				    	}
						if(prevlets.length%2 == 0){
							result+="a";
						}
						else{
							result+="1";
						}
						String current = CodeIn.getText();
						CodeIn.setText(current.substring(0,current.lastIndexOf('\n'))+"\n"+result+") ");
					}
				}
			}
			
			
			
			

			
		});
    }
    
    private void displayNodes(NoteNode node,int deapth) {
		String s = "";
		for(int i = 1; i < deapth; i ++){
			s+="\t";
		}
		if(node.value != null){
			s+=(char)8226+node.value+"\n";
			TextOut.appendText(s);
		}
		for(NoteNode n :node.children){
			if(n != null){
				displayNodes(n,deapth+1);
			}
		}
		
	}
    private String[] ParseLetters(String firstword) {
		ArrayList<String> result = new ArrayList<String>();
		Boolean isLetter = true;
		int groupstart = 0;
		int count = 0;
		for(char c: firstword.toCharArray()){
			if(isLetter){
				if(Character.isDigit(c)){
					isLetter = false;
					result.add(firstword.substring(groupstart,count));
					groupstart = count;
				}
			}
			else{
				if(Character.isLetter(c)){
					isLetter = true;
					result.add(firstword.substring(groupstart,count));
					groupstart = count;
				}
			}
			count++;
		}
		result.add(firstword.substring(groupstart,firstword.length()-1));
		String[] resulting = result.toArray(new String[0]);
		prevlets = resulting;
		return resulting;
	}

    @FXML
    private AnchorPane Diagram;


    public static void main(String[] args) {
		Application.launch(args);
	}
    

	@Override
	public void start(Stage primaryStage) throws Exception {
		FXMLLoader loader = new FXMLLoader(this.getClass().getResource("Notes.fxml"));
		Pane page = (Pane) loader.load();
		Scene scene = new Scene(page);
		primaryStage.setScene(scene);
		//primaryStage.initStyle(StageStyle.UNDECORATED);
		primaryStage.show();
	}
	
	@FXML
    void exitbutton(ActionEvent event) {
       System.exit(0);
	}
  @FXML
    void savebutton(ActionEvent event) {
	  try(  PrintWriter out = new PrintWriter(saveName+ ".txt" )  ){
          out.print( yourText );
          out.close();
		} 
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
    }

    @FXML
    void loadbutton(ActionEvent event) {
    	CodeIn.setText("");
    	TextOut.setText("");
    	QuestionsArea.setText("");
    	CommentsArea.setText("");
    	try {
			Scanner in = new Scanner(new FileReader(saveName+ ".txt"));
			while(in.hasNext()){
				String s= in.nextLine();
				System.out.println(s);
				CodeIn.appendText(s+"\n");
				processString(s+"\n");
			}
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
    	
    	
    }
}

class NoteNode{
	public ArrayList<NoteNode> children = new ArrayList<NoteNode>();
	public String value = "";
	private NoteNode parent;
	
	public NoteNode(String value,ArrayList<NoteNode> children,NoteNode parent){
		this.value = value;
		this.children = children;
		this.parent = parent;
	}
	
	public String getLocation(){
		ArrayList<Integer> ints = new ArrayList<Integer>();
		NoteNode current = this;
		while(current.parent != null){
			ints.add(current.parent.children.indexOf(current));
		}
		String result = "";
		for(int i = ints.size(); i <= 0; i --){
			if(i % 2 == 0){
				result = getString(ints.get(i))+ result;
			}
			else{
				result = (ints.get(i)+1)+ result;
			}
		}
		return null;
	}
	
	public static int convertString(String s){
		int result = 0;
		int count = 0;
		for(char c: s.toLowerCase().toCharArray()){
			result+= ((int) c - 96)* Math.pow(26, s.length()-1-count);
			count++;
		}
		return result;
	}
	
	public static String getString(int i){
		String s = "";
		while(i > 26){
			s = ((char)((i % 26) + 96))+s;
			i = i/26;
		}
		s = ((char)((i % 26) + 96))+s;
		return s;
	}
	
	public String toString(){
		return value;
	}
}


