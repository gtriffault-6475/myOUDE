package com.renault.oude.portfolio;

import java.time.LocalDateTime;
import java.util.List;

public record PortefeuilleResponse(
    List<Lead> leads,
    List<Affaire> affaires,
    boolean donneesDatees,
    LocalDateTime derniereSynchronisation
) {}
