package net.shrine.config

/**
 * @author clint
 * @date Feb 6, 2013
 */
final case class HappyShrineConfig(
    isAdapter: Boolean,
    pmEndpoint: String,
    broadcasterPeerGroupToQuery: String,
    adapterStatusQuery: String)