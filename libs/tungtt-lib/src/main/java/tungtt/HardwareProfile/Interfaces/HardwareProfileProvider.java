package tungtt.HardwareProfile.Interfaces;

import tungtt.HardwareProfile.Contexts.HardwareSnapshot;
    
public interface HardwareProfileProvider {
    
    HardwareSnapshot snapshot();
}