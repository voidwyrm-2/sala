package sala;

public class SalaEntry {
    public final Instruction instruction;
    public final EntryEffect effect;

    public SalaEntry(Instruction instruction, EntryEffect effect) {
        this.instruction = instruction;
        this.effect = effect;
    }
}
