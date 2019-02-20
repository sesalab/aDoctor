package adoctor.application.ast.visitor;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;

import java.util.ArrayList;

public class BlockVisitor extends ASTVisitor {

    private ArrayList<Block> blockList;

    public BlockVisitor(ArrayList<Block> blockList) {
        this.blockList = blockList;
    }

    @Override
    public boolean visit(Block blockNode) {
        blockList.add(blockNode);
        return true;
    }
}
