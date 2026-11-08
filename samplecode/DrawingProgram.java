import javax.swing.*;
public class DrawingProgram {
  public static void main(String[] args){
    JFrame.setDefaultLookAndFeelDecorated(true);
    Model model = new Model();
    UndoManager undoManager = new UndoManager();
    UndoManager.setModel(model);
    View.setUndoManager(undoManager);
    View.setModel(model);
    View view = new View();
    Model.setView(view);
    Command.setUndoManager(undoManager);
    Command.setModel(model);
    view.show();
  }
}